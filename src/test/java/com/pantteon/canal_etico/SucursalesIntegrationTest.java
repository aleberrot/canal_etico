package com.pantteon.canal_etico;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pantteon.canal_etico.model.*;
import com.pantteon.canal_etico.repository.*;
import com.pantteon.canal_etico.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(properties={
 "spring.profiles.active=test", "spring.datasource.url=jdbc:h2:mem:branches;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
 "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=",
 "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false", "ADMIN_PASSWORD=integration-only",
 "app.clients.mathiesen.enabled=false", "app.demo-companies.enabled=false", "app.storage.type=local",
 "app.notifications.denuncias-recipient=synthetic@example.invalid"
})
@AutoConfigureMockMvc @Transactional
class SucursalesIntegrationTest {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper json;
 @Autowired EmpresaRepository companies;
 @Autowired SucursalRepository branches;
 @Autowired DenunciaRepository reports;
 @MockitoBean EmailService email;
 @MockitoBean FileStorageService files;
 final String auth="Basic "+Base64.getEncoder().encodeToString("admin:integration-only".getBytes(java.nio.charset.StandardCharsets.UTF_8));
 @BeforeEach void seed(){
  Empresa e=new Empresa();e.setCodigo("mathiesen");e.setNombre("Grupo Mathiesen");e.setCorreoNotificacion("synthetic@example.invalid");companies.saveAndFlush(e);
  Sucursal s=new Sucursal();s.setEmpresa(e);s.setCodigo("chile");s.setNombre("Mathiesen Chile");branches.saveAndFlush(s);
  Empresa other=new Empresa();other.setCodigo("other");other.setNombre("Otra");other.setCorreoNotificacion("synthetic@example.invalid");companies.saveAndFlush(other);
  Sucursal foreign=new Sucursal();foreign.setEmpresa(other);foreign.setCodigo("foreign");foreign.setNombre("Sucursal ajena");branches.saveAndFlush(foreign);
 }
 byte[] request(String type,String branch) throws Exception {
  var data=new HashMap<String,Object>();data.put("codigoEmpresa","mathiesen");data.put("codigoSucursal",branch);
  data.put("tipoDenuncia",type);data.put("delitoCodigo","DEL-001");data.put("delitoTipificacion","Prueba");
  data.put("relacionEmpresa","TRABAJADOR");data.put("descripcion","Descripción sintética para pruebas aisladas de integración.");
  data.put("nombreDenunciante","Persona sintética");data.put("emailDenunciante","synthetic@example.invalid");data.put("telefonoDenunciante","111111111");
  data.put("password","synthetic-password");data.put("terminosAceptados",true);data.put("versionTerminos","synthetic-version");
  return json.writeValueAsBytes(data);
 }
 @Test void publicCatalogIsScopedAndAdministrationRequiresAuthentication() throws Exception {
  mvc.perform(get("/api/empresas/mathiesen/sucursales")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].codigo").value("chile"));
  mvc.perform(get("/api/admin/empresas/mathiesen/sucursales")).andExpect(status().isUnauthorized());
  mvc.perform(get("/api/admin/empresas/mathiesen/sucursales").header("Authorization",auth)).andExpect(status().isOk());
 }
 @Test void adminCreatesEditsAndDeactivatesWithoutRemovingHistory() throws Exception {
  mvc.perform(post("/api/admin/empresas/mathiesen/sucursales").header("Authorization",auth).contentType("application/json").content("{\"codigo\":\"nueva\",\"nombre\":\"Nueva\",\"activa\":true}")).andExpect(status().isCreated());
  mvc.perform(put("/api/admin/empresas/mathiesen/sucursales/nueva").header("Authorization",auth).contentType("application/json").content("{\"codigo\":\"nueva\",\"nombre\":\"Nombre editado\",\"activa\":false}")).andExpect(status().isOk());
  assertFalse(branches.findByEmpresaCodigoAndCodigo("mathiesen","nueva").orElseThrow().isActiva());
  mvc.perform(get("/api/empresas/mathiesen/sucursales")).andExpect(jsonPath("$.length()").value(1));
 }
 @Test void bothReportTypesPersistLocationAndTrackingKeepsOriginalName() throws Exception {
  for(String type:List.of("GENERAL","LEY_KARIN")){
   var result=mvc.perform(multipart("/api/denuncias").file(new MockMultipartFile("data","data.json","application/json",request(type,"chile")))).andExpect(status().isCreated()).andReturn();
   String code=json.readTree(result.getResponse().getContentAsString()).get("codigo").asText();
   var saved=reports.findByCodigoUnico(code).orElseThrow();assertEquals("chile",saved.getSucursal().getCodigo());assertEquals("Mathiesen Chile",saved.getSucursalNombre());
   var branch=branches.findByEmpresaCodigoAndCodigo("mathiesen","chile").orElseThrow();branch.setNombre("Nombre posterior");branches.saveAndFlush(branch);
   mvc.perform(post("/api/denuncias/seguimiento").contentType("application/json").content(json.writeValueAsBytes(Map.of("code",code,"password","synthetic-password")))).andExpect(status().isOk()).andExpect(jsonPath("$.sucursalNombre").value("Mathiesen Chile"));
   branch.setNombre("Mathiesen Chile");branches.saveAndFlush(branch);
  }
 }
 @Test void invalidLocationCannotCreateReport() throws Exception {
  for(String code:List.of("foreign","missing","")){
   mvc.perform(multipart("/api/denuncias").file(new MockMultipartFile("data","data.json","application/json",request("GENERAL",code)))).andExpect(status().is4xxClientError());
  }
  var branch=branches.findByEmpresaCodigoAndCodigo("mathiesen","chile").orElseThrow();branch.setActiva(false);branches.saveAndFlush(branch);
  mvc.perform(multipart("/api/denuncias").file(new MockMultipartFile("data","data.json","application/json",request("LEY_KARIN","chile")))).andExpect(status().is4xxClientError());
  assertEquals(0,reports.count());
 }
}
