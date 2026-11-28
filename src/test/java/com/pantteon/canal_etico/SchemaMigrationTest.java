package com.pantteon.canal_etico;

import java.sql.DriverManager;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import static org.junit.jupiter.api.Assertions.*;

class SchemaMigrationTest {
 @Test void packagedMigrationIsAdditiveIdempotentAndPreservesHistoricDates() throws Exception {
  var resource=new ClassPathResource("db/20261124_observaciones.sql");
  assertTrue(resource.exists(),"The same reviewed operations script must be packaged in the JAR");
  try(var connection=DriverManager.getConnection("jdbc:h2:mem:migration-"+UUID.randomUUID()+";MODE=PostgreSQL","sa","")){
   try(var statement=connection.createStatement()){
    statement.execute("CREATE TABLE denuncias (id UUID PRIMARY KEY, created_at DATE, updated_at DATE)");
    statement.execute("INSERT INTO denuncias VALUES (RANDOM_UUID(),DATE '2026-11-20',DATE '2026-11-21')");
   }
   ScriptUtils.executeSqlScript(connection,resource);
   ScriptUtils.executeSqlScript(connection,resource);
   try(var statement=connection.createStatement();var result=statement.executeQuery("SELECT created_at,updated_at,registered_at,last_activity_at FROM denuncias")){
    assertTrue(result.next());
    assertEquals(LocalDate.of(2026,11,20),result.getObject(1,LocalDate.class));
    assertEquals(LocalDate.of(2026,11,21),result.getObject(2,LocalDate.class));
    assertNull(result.getObject(3));assertNull(result.getObject(4));assertFalse(result.next());
   }
   try(var statement=connection.createStatement();var result=statement.executeQuery("SELECT COUNT(*) FROM notificaciones_denuncia")){
    assertTrue(result.next());assertEquals(0,result.getLong(1));
   }
  }
 }
 @Test void migrationRequiresExplicitActivationAndDoesNotDisableHibernateValidation() throws Exception {
  var properties=new java.util.Properties();
  try(var input=new ClassPathResource("application.properties").getInputStream()){properties.load(input);}
  assertEquals("${SCHEMA_MIGRATION_MODE:never}",properties.getProperty("spring.sql.init.mode"));
  assertEquals("false",properties.getProperty("spring.jpa.defer-datasource-initialization"));
  assertEquals("false",properties.getProperty("spring.sql.init.continue-on-error"));
  var prod=new java.util.Properties();
  try(var input=new ClassPathResource("application-prod.properties").getInputStream()){prod.load(input);}
  assertEquals("validate",prod.getProperty("spring.jpa.hibernate.ddl-auto"));
 }
}
