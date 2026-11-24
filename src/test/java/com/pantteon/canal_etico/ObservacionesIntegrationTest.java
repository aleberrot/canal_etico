package com.pantteon.canal_etico;
import com.pantteon.canal_etico.model.*;
import com.pantteon.canal_etico.repository.*;
import com.pantteon.canal_etico.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.junit.jupiter.api.*;
import java.time.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class ObservacionesIntegrationTest extends SucursalesIntegrationTest {
 @Autowired NotificacionRepository notifications;
 @Autowired AporteDenunciaRepository contributions;
 @Autowired NotificationQueue queue;
 @Autowired NotificationDispatcher dispatcher;
 @MockitoBean Clock clock;
 Instant now=Instant.parse("2026-11-24T00:30:00Z");
 @BeforeEach void fixedClock(){when(clock.instant()).thenReturn(now);}
 String create(String type) throws Exception {
  var result=mvc.perform(multipart("/api/denuncias").file(new MockMultipartFile("data","data.json","application/json",request(type,"chile"))))
   .andExpect(status().isCreated()).andReturn();
  return json.readTree(result.getResponse().getContentAsString()).get("codigo").asText();
 }
 String contribute(String code,String text,boolean file) throws Exception {
  var req=multipart("/api/denuncias/"+code+"/informacion-adicional")
   .file(new MockMultipartFile("password","","text/plain","synthetic-password".getBytes()));
  if(text!=null)req.file(new MockMultipartFile("informacion","","text/plain",text.getBytes()));
  if(file)req.file(new MockMultipartFile("archivo","synthetic.txt","text/plain","synthetic bytes".getBytes()));
  return mvc.perform(req).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
 }
 @Test void generalAndKarinKeepEveryContributionAndQueueEveryEvent() throws Exception {
  when(files.guardarArchivo(any(),anyString(),anyString())).thenReturn("synthetic/unique.txt");
  for(String type:List.of("GENERAL","LEY_KARIN")){
   String code=create(type);
   var saved=reports.findByCodigoUnico(code).orElseThrow();
   assertEquals(LocalDate.of(2026,11,23),saved.getCreatedAt());assertEquals(now,saved.getRegisteredAt());
   contribute(code,"Primer antecedente sintético",true);
   when(clock.instant()).thenReturn(now.plusSeconds(1));
   var result=json.readTree(contribute(code,"Segundo antecedente sintético",false));
   assertEquals(2,result.get("aportes").size());assertNotEquals(result.at("/aportes/0/id").asText(),result.at("/aportes/1/id").asText());
   assertEquals("Primer antecedente sintético",result.at("/aportes/0/informacion").asText());assertFalse(result.at("/aportes/1/tieneArchivo").asBoolean());
   assertEquals("2026-11-23",result.get("createdAt").asText());assertEquals(now.toString(),result.get("createdAtInstant").asText());
   mvc.perform(get("/api/admin/empresas/mathiesen/denuncias").header("Authorization",auth)).andExpect(status().isOk());
   queue.nueva(saved,null,now); // Same creation event cannot be queued twice.
   when(clock.instant()).thenReturn(now);
  }
  assertEquals(2,reports.count());assertEquals(4,contributions.count());assertEquals(6,notifications.count());
  assertEquals(6,notifications.findAll().stream().map(NotificacionDenuncia::getEvento).distinct().count());
 }
 @Test void smtpFailureDoesNotDuplicateStoredReportsOrContributions() throws Exception {
  String code=create("LEY_KARIN");contribute(code,"Antecedente",false);
  when(email.configurada()).thenReturn(true);doThrow(new IllegalStateException("synthetic SMTP failure")).when(email).enviar(any());
  dispatcher.procesarPendientes();
  assertEquals(1,reports.count());assertEquals(1,contributions.count());assertEquals(2,notifications.count());
  assertTrue(notifications.findAll().stream().allMatch(n->n.getEstado()==NotificacionDenuncia.Estado.PENDIENTE));
  reset(email);when(email.configurada()).thenReturn(true);when(clock.instant()).thenReturn(now.plusSeconds(61));
  dispatcher.procesarPendientes();dispatcher.procesarPendientes();
  verify(email,times(2)).enviar(any());assertTrue(notifications.findAll().stream().allMatch(n->n.getEstado()==NotificacionDenuncia.Estado.ENVIADA));
 }
 @Test void karinFileDownloadNeedsCorrectPasswordAndOwnership() throws Exception {
  String a=create("LEY_KARIN"),b=create("GENERAL");
  when(files.guardarArchivo(any(),anyString(),anyString())).thenReturn("private/unique.txt");
  var result=json.readTree(contribute(a,null,true));String id=result.at("/aportes/0/id").asText();
  String path="/api/denuncias/"+a+"/aportes/"+id+"/archivo";
  mvc.perform(post(path).contentType("application/json").content("{\"password\":\"incorrecta\"}")).andExpect(status().isUnauthorized());
  mvc.perform(post("/api/denuncias/"+b+"/aportes/"+id+"/archivo").contentType("application/json").content("{\"password\":\"synthetic-password\"}")).andExpect(status().isNotFound());
  when(files.cargarArchivo("private/unique.txt")).thenReturn(new ByteArrayResource("synthetic bytes".getBytes()));
  mvc.perform(post(path).contentType("application/json").content("{\"password\":\"synthetic-password\"}"))
   .andExpect(status().isOk()).andExpect(content().bytes("synthetic bytes".getBytes()));
 }
 @Test void invalidContributionDoesNotLeaveOrphanNotification() throws Exception {
  String code=create("LEY_KARIN");
  mvc.perform(multipart("/api/denuncias/"+code+"/informacion-adicional").file(new MockMultipartFile("password","","text/plain","synthetic-password".getBytes())))
   .andExpect(status().isConflict());assertEquals(1,notifications.count());assertEquals(0,contributions.count());
 }
}
