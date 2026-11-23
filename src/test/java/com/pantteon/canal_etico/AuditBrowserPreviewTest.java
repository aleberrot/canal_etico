package com.pantteon.canal_etico;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pantteon.canal_etico.model.*;
import com.pantteon.canal_etico.repository.*;
import com.pantteon.canal_etico.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Explicit local browser harness. Never uses Aiven, real mail, or production files. */
@EnabledIfSystemProperty(named="canal.audit.preview", matches="true")
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.DEFINED_PORT,properties={
 "spring.profiles.active=test", "server.port=18081", "server.address=127.0.0.1",
 "spring.datasource.url=jdbc:h2:mem:browseraudit;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
 "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=",
 "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false", "ADMIN_PASSWORD=integration-only",
 "FRONTEND_ORIGIN=http://localhost:5173", "app.clients.mathiesen.enabled=false", "app.demo-companies.enabled=false",
 "app.storage.type=local", "app.notifications.denuncias-recipient=synthetic@example.invalid"
})
class AuditBrowserPreviewTest {
 @Autowired EmpresaRepository companies;
 @Autowired SucursalRepository branches;
 @Autowired ObjectMapper json;
 @MockitoBean EmailService email;
 @MockitoBean FileStorageService storage;

 @Test void serveIsolatedBrowserSession() throws Exception {
  Map<String,Resource> uploads=new ConcurrentHashMap<>();
  when(storage.guardarArchivo(any(),anyString(),anyString())).thenAnswer(inv -> {
   MultipartFile file=inv.getArgument(0);String filename=file.getOriginalFilename();
   String key=inv.getArgument(1)+"/"+inv.getArgument(2)+"/"+UUID.randomUUID()+"-"+filename;
   byte[] bytes=file.getBytes();uploads.put(key,new ByteArrayResource(bytes){@Override public String getFilename(){return filename;}});return key;
  });
  when(storage.cargarArchivo(anyString())).thenAnswer(inv -> uploads.get(inv.getArgument(0)));
  for(String code:List.of("mathiesen","emp-01","emp-02")){
   Empresa company=new Empresa();company.setCodigo(code);company.setNombre(code.equals("mathiesen")?"Grupo Mathiesen":code.equals("emp-01")?"Empresa 1":"Empresa 2");
   company.setCorreoNotificacion("synthetic@example.invalid");company.setLogo(code.equals("mathiesen")?"/empresas/mathiesen/logo.png":code.equals("emp-01")?"/empresas/emp-01/logo.webp":null);
   companies.saveAndFlush(company);
   if(code.equals("mathiesen")) for(var row:json.readTree(Files.readString(Path.of("docs/mathiesen-sucursales.json")))){
    Sucursal branch=new Sucursal();branch.setEmpresa(company);branch.setCodigo(row.get("codigo").asText());branch.setNombre(row.get("nombre").asText());branches.saveAndFlush(branch);
   }
  }
  System.out.println("AUDIT_BROWSER_READY: local H2, 17 branches, mocked mail and files, http://127.0.0.1:18081");
  long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.MINUTES.toNanos(25);
  while(System.nanoTime()<deadline && !Files.exists(Path.of("target/audit-stop"))) Thread.sleep(500);
 }
}
