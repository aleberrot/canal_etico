package com.pantteon.canal_etico;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pantteon.canal_etico.model.*;
import com.pantteon.canal_etico.repository.*;
import com.pantteon.canal_etico.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={
 "spring.profiles.active=test","spring.datasource.url=jdbc:h2:mem:sprint;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
 "spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.username=sa","spring.datasource.password=",
 "spring.jpa.hibernate.ddl-auto=create-drop","ADMIN_PASSWORD=integration-only","app.clients.mathiesen.enabled=false",
 "app.demo-companies.enabled=false","app.storage.type=local","app.notifications.enabled=false",
 "app.notifications.recipients=one@example.invalid,two@example.invalid,three@example.invalid",
 "app.notifications.from=notifications@example.invalid","spring.mail.properties.mail.smtp.auth=false",
 "spring.mail.properties.mail.smtp.starttls.enable=false","spring.mail.properties.mail.smtp.starttls.required=false",
 "spring.mail.properties.mail.smtp.ssl.enable=false"
})
@AutoConfigureMockMvc
class SprintClosureIntegrationTest {
 static final LoopbackSmtp smtp;
 static {try{smtp=new LoopbackSmtp();}catch(Exception e){throw new ExceptionInInitializerError(e);}}
 @DynamicPropertySource static void smtpPort(DynamicPropertyRegistry properties){
  properties.add("spring.mail.host",()->"127.0.0.1");properties.add("spring.mail.port",smtp::port);
 }
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper json;
 @Autowired EmpresaRepository companies;
 @Autowired SucursalRepository branches;
 @Autowired DenunciaRepository reports;
 @Autowired AporteDenunciaRepository contributions;
 @Autowired NotificacionRepository events;
 @Autowired NotificationDispatcher dispatcher;
 @Autowired TransactionTemplate tx;
 @MockitoBean Clock clock;
 @MockitoBean FileStorageService files;
 final Instant now=Instant.parse("2026-11-26T20:00:00Z");
 @BeforeEach void seed(){
  when(clock.instant()).thenReturn(now);smtp.messages.clear();smtp.errors.clear();smtp.rejectNext.set(false);
  tx.executeWithoutResult(status->{
   events.deleteAll();contributions.deleteAll();reports.deleteAll();branches.deleteAll();companies.deleteAll();companies.flush();
   var company=new Empresa();company.setCodigo("synthetic");company.setNombre("Empresa sintética");company.setCorreoNotificacion("synthetic@example.invalid");companies.save(company);
  });
  when(files.guardarArchivo(any(),anyString(),anyString())).thenAnswer(i->"private/"+UUID.randomUUID()+"/synthetic.txt");
 }
 @AfterAll static void closeSink() throws Exception {smtp.close();}
 String create(String type) throws Exception {
  var data=new HashMap<String,Object>();data.put("codigoEmpresa","synthetic");data.put("tipoDenuncia",type);
  data.put("delitoCodigo","DEL-001");data.put("delitoTipificacion","Prueba");data.put("relacionEmpresa","TRABAJADOR");
  data.put("descripcion","Contenido privado sintético que no debe enviarse por correo.");
  data.put("nombreDenunciante","Persona sintética privada");data.put("emailDenunciante","private@example.invalid");data.put("telefonoDenunciante","111111111");
  data.put("password","synthetic-password");data.put("terminosAceptados",true);data.put("versionTerminos","synthetic-version");
  var response=mvc.perform(multipart("/api/denuncias").file(new MockMultipartFile("data","data.json","application/json",json.writeValueAsBytes(data))))
   .andExpect(status().isCreated()).andReturn().getResponse();
  return json.readTree(response.getContentAsString()).get("codigo").asText();
 }
 com.fasterxml.jackson.databind.JsonNode contribute(String code,String text,boolean file) throws Exception {
  var request=multipart("/api/denuncias/"+code+"/informacion-adicional")
   .file(new MockMultipartFile("password","","text/plain","synthetic-password".getBytes()));
  if(text!=null)request.file(new MockMultipartFile("informacion","","text/plain",text.getBytes()));
  if(file)request.file(new MockMultipartFile("archivo","synthetic.txt","text/plain","private synthetic file".getBytes()));
  return json.readTree(mvc.perform(request).andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
 }
 @Test void bothTypesCommitThreeIndependentContributionsAndSendAllFourKindsThroughSmtp() throws Exception {
  for(String type:List.of("GENERAL","LEY_KARIN")){
   String code=create(type);
   mvc.perform(post("/api/denuncias/seguimiento").contentType("application/json").content(json.writeValueAsBytes(Map.of("code",code,"password","synthetic-password"))))
    .andExpect(status().isOk()).andExpect(jsonPath("$.aportes.length()").value(0));
   var ids=new HashSet<String>();
   for(int i=0;i<3;i++){
    when(clock.instant()).thenReturn(now.plusSeconds(i+1));
    var result=contribute(code,i==1?null:"Aporte privado sintético "+i,i>0);
    assertEquals(i+1,result.get("aportes").size());ids.add(result.at("/aportes/"+i+"/id").asText());
   }
   assertEquals(3,ids.size());
  }
  assertEquals(2,reports.count());assertEquals(6,contributions.count());assertEquals(8,events.count());
  when(clock.instant()).thenReturn(now.plusSeconds(10));dispatcher.procesarPendientes();dispatcher.procesarPendientes();
  assertEquals(8,smtp.messages.size());assertEquals(8,events.countByEstado(NotificacionDenuncia.Estado.ENVIADA));
  var subjects=new HashMap<String,Integer>();var ids=new HashSet<String>();
  for(String raw:smtp.messages){
   var message=new jakarta.mail.internet.MimeMessage(jakarta.mail.Session.getInstance(new Properties()),new java.io.ByteArrayInputStream(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
   subjects.merge(message.getSubject(),1,Integer::sum);ids.add(message.getMessageID());
   assertEquals(Set.of("one@example.invalid","two@example.invalid","three@example.invalid"),Arrays.stream(message.getAllRecipients()).map(a->((jakarta.mail.internet.InternetAddress)a).getAddress()).collect(java.util.stream.Collectors.toSet()));
   String body=decoded(message);assertTrue(body.contains("26/11/2026 17:00:"));assertTrue(body.contains("Empresa sintética"));
   for(String secret:List.of("Contenido privado sintético","Persona sintética privada","private@example.invalid","synthetic-password","Aporte privado sintético","private synthetic file"))assertFalse(body.contains(secret));
  }
  assertEquals(Map.of("Detalle de la Denuncia General",1,"Detalle de la Denuncia Karin",1,"Nuevos antecedentes agregados a una Denuncia General",3,"Nuevos antecedentes agregados a una Denuncia Karin",3),subjects);
  assertEquals(8,ids.size());assertTrue(smtp.errors.isEmpty());
 }
 @Test void temporarySmtpFailureRetainsCommittedDataAndRetriesWithoutDuplicateReportOrMail() throws Exception {
  String code=create("LEY_KARIN");contribute(code,"Antecedente privado",false);
  smtp.rejectNext.set(true);dispatcher.procesarPendientes();
  assertEquals(1,reports.count());assertEquals(1,contributions.count());assertEquals(2,events.count());
  assertEquals(1,events.countByEstado(NotificacionDenuncia.Estado.PENDIENTE));assertEquals(1,smtp.messages.size());
  dispatcher.procesarPendientes();assertEquals(1,smtp.messages.size());
  when(clock.instant()).thenReturn(now.plusSeconds(61));dispatcher.procesarPendientes();dispatcher.procesarPendientes();
  assertEquals(2,smtp.messages.size());assertEquals(2,events.countByEstado(NotificacionDenuncia.Estado.ENVIADA));
  assertEquals(1,reports.count());assertEquals(1,contributions.count());assertTrue(smtp.errors.isEmpty());
 }
 @Test void concurrentDispatchersDoNotSendTheSamePendingEventTwice() throws Exception {
  create("GENERAL");var executor=Executors.newFixedThreadPool(2);var start=new CountDownLatch(1);
  try{
   var first=executor.submit(()->{start.await();dispatcher.procesarPendientes();return true;});
   var second=executor.submit(()->{start.await();dispatcher.procesarPendientes();return true;});start.countDown();
   assertTrue(first.get(20,TimeUnit.SECONDS));assertTrue(second.get(20,TimeUnit.SECONDS));
   assertEquals(1,smtp.messages.size());assertEquals(1,events.countByEstado(NotificacionDenuncia.Estado.ENVIADA));
  }finally{executor.shutdownNow();}
 }
 @Test void invalidContributionCredentialsAndMissingReportsLeaveNoEventsOrFiles() throws Exception {
  for(String type:List.of("GENERAL","LEY_KARIN")){
   String code=create(type);
   mvc.perform(multipart("/api/denuncias/"+code+"/informacion-adicional")
    .file(new MockMultipartFile("password","","text/plain","incorrect".getBytes()))
    .file(new MockMultipartFile("informacion","","text/plain","synthetic".getBytes())))
    .andExpect(status().isUnauthorized());
  }
  mvc.perform(multipart("/api/denuncias/DEN-MISSING/informacion-adicional")
   .file(new MockMultipartFile("password","","text/plain","incorrect".getBytes()))
   .file(new MockMultipartFile("informacion","","text/plain","synthetic".getBytes())))
   .andExpect(status().isNotFound());
  assertEquals(0,contributions.count());assertEquals(2,events.count());verifyNoInteractions(files);
 }
 private static String decoded(jakarta.mail.Part part) throws Exception {
  if(part.isMimeType("text/*"))return part.getContent().toString();
  if(part.getContent() instanceof jakarta.mail.Multipart multipart){var body=new StringBuilder();for(int i=0;i<multipart.getCount();i++)body.append(decoded(multipart.getBodyPart(i)));return body.toString();}
  return "";
 }
}
