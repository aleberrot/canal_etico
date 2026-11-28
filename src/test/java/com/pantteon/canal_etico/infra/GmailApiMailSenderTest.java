package com.pantteon.canal_etico.infra;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pantteon.canal_etico.model.*;
import com.pantteon.canal_etico.service.*;
import com.sun.net.httpserver.HttpServer;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.net.*;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GmailApiMailSenderTest {
 private HttpServer server;
 private final ObjectMapper json=new ObjectMapper();
 private final AtomicInteger tokens=new AtomicInteger();
 private final List<MimeMessage> delivered=new ArrayList<>();
 private int tokenStatus=200,sendStatus=200;
 private String tokenBody="{\"access_token\":\"synthetic-access-token\",\"token_type\":\"Bearer\",\"expires_in\":3600}";
 private String sendBody="{\"id\":\"synthetic-message-id\"}";
 private String postedForm;
 private final MutableClock clock=new MutableClock();

 @BeforeEach void start() throws Exception {
  server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
  server.createContext("/token",exchange->{
   tokens.incrementAndGet();postedForm=new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8);
   byte[] body=tokenBody.getBytes(StandardCharsets.UTF_8);exchange.sendResponseHeaders(tokenStatus,body.length);
   exchange.getResponseBody().write(body);exchange.close();
  });
  server.createContext("/send",exchange->{
   try{
    assertEquals("POST",exchange.getRequestMethod());
    assertEquals("Bearer synthetic-access-token",exchange.getRequestHeaders().getFirst("Authorization"));
    var raw=json.readTree(exchange.getRequestBody()).path("raw").asText();
    delivered.add(new MimeMessage(Session.getInstance(new Properties()),new java.io.ByteArrayInputStream(Base64.getUrlDecoder().decode(raw))));
    byte[] body=sendBody.getBytes(StandardCharsets.UTF_8);exchange.sendResponseHeaders(sendStatus,body.length);
    exchange.getResponseBody().write(body);
   }catch(Exception error){throw new RuntimeException(error);}finally{exchange.close();}
  });server.start();
 }
 @AfterEach void stop(){server.stop(0);}
 private GmailApiMailSender sender(String refresh){
  String base="http://127.0.0.1:"+server.getAddress().getPort();
  return new GmailApiMailSender("synthetic-client-id","synthetic-secret",refresh,"sender@example.invalid",json,
   HttpClient.newHttpClient(),clock,URI.create(base+"/token"),URI.create(base+"/send"));
 }
 @SuppressWarnings("unchecked") private EmailService email(GmailApiMailSender sender,String from){
  ObjectProvider<JavaMailSender> provider=mock(ObjectProvider.class);
  when(provider.getIfAvailable()).thenReturn(sender);when(provider.getObject()).thenReturn(sender);
  return new EmailService(provider,new BusinessTime(clock),"one@example.invalid,two@example.invalid,three@example.invalid","https://example.invalid/admin",from);
 }
 private NotificacionDenuncia event(TipoDenuncia tipo,boolean aporte){
  var n=new NotificacionDenuncia();n.setEmpresa("Empresa sintética");n.setSucursal("Sucursal sintética");
  n.setCodigo("DEN-SYNTHETIC");n.setTipo(tipo);n.setAporte(aporte);n.setOcurridoAt(clock.instant());return n;
 }
 @Test void fourNotificationTypesUseHttpsMimeWithoutSmtpAndCacheToken() throws Exception {
  var sender=sender("synthetic-refresh-token");var email=email(sender,"");assertTrue(email.configurada());
  var subjects=new HashSet<String>();
  for(var tipo:TipoDenuncia.values())for(boolean aporte:new boolean[]{false,true})email.enviar(event(tipo,aporte));
  assertEquals(4,delivered.size());assertEquals(1,tokens.get());
  assertTrue(postedForm.contains("grant_type=refresh_token"));assertTrue(postedForm.contains("refresh_token=synthetic-refresh-token"));
  for(var message:delivered){
   subjects.add(message.getSubject());assertEquals(3,message.getAllRecipients().length);
   assertEquals("sender@example.invalid",message.getFrom()[0].toString());
   assertNotNull(message.getHeader("X-WEBETIKO-Event-ID"));
   assertTrue(message.getHeader("Message-ID")[0].endsWith("@example.invalid>"));
   var bytes=new java.io.ByteArrayOutputStream();message.writeTo(bytes);
   String raw=bytes.toString(StandardCharsets.UTF_8);assertTrue(raw.contains("multipart/alternative"));
   assertFalse(raw.contains("synthetic-secret"));assertFalse(raw.contains("synthetic-refresh-token"));
  }
  assertEquals(Set.of("Detalle de la Denuncia General","Detalle de la Denuncia Karin","Nuevos antecedentes agregados a una Denuncia General","Nuevos antecedentes agregados a una Denuncia Karin"),subjects);
 }
 @Test void accessTokenRenewsAfterExpiry() throws Exception {
  var email=email(sender("synthetic-refresh-token"),"");email.enviar(event(TipoDenuncia.GENERAL,false));
  clock.now=clock.now.plusSeconds(3600);email.enviar(event(TipoDenuncia.LEY_KARIN,true));
  assertEquals(2,tokens.get());assertEquals(2,delivered.size());
 }
 @Test void revokedAuthorizationDoesNotSendOrExposeResponseSecrets(){
  tokenStatus=400;tokenBody="{\"error\":\"invalid_grant\",\"error_description\":\"synthetic-secret synthetic-refresh-token\"}";
  var failure=assertThrows(GmailApiMailSender.DeliveryException.class,()->email(sender("synthetic-refresh-token"),"").enviar(event(TipoDenuncia.GENERAL,false)));
  assertEquals("GMAIL_API_AUTHORIZATION_REQUIRED",failure.code());assertEquals(failure.code(),failure.getMessage());
  assertNull(failure.getCause());assertEquals(0,delivered.size());
 }
 @Test void quotaFailureIsNotReportedAsSentOrRetriedInsideTransport(){
  sendStatus=429;sendBody="{\"error\":\"synthetic-secret\"}";
  var failure=assertThrows(GmailApiMailSender.DeliveryException.class,()->email(sender("synthetic-refresh-token"),"").enviar(event(TipoDenuncia.GENERAL,false)));
  assertEquals("GMAIL_API_RATE_LIMITED",failure.code());assertEquals(1,delivered.size());
  assertFalse(failure.toString().contains("synthetic-secret"));
 }
 @Test void incompleteAuthorizationAndWrongFromStayPausedWithoutNetworkCalls(){
  assertEquals("GMAIL_API_AUTHORIZATION_MISSING",email(sender(""),"").problemaConfiguracion());
  assertEquals("GMAIL_API_FROM_MISMATCH",email(sender("synthetic-refresh-token"),"other@example.invalid").problemaConfiguracion());
  assertEquals(0,tokens.get());assertTrue(delivered.isEmpty());
 }
 @Test void gmailBeanReplacesSmtpEvenWhenLegacySmtpHostIsPresent(){
  new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(MailSenderAutoConfiguration.class))
   .withUserConfiguration(GmailApiConfiguration.class).withBean(ObjectMapper.class,ObjectMapper::new)
   .withPropertyValues("app.notifications.transport=gmail-api","spring.mail.host=smtp.example.invalid")
   .run(context->{assertNull(context.getStartupFailure());assertEquals(1,context.getBeansOfType(JavaMailSender.class).size());
    assertInstanceOf(GmailApiMailSender.class,context.getBean(JavaMailSender.class));});
 }
 private static class MutableClock extends Clock {
  Instant now=Instant.parse("2026-11-28T17:00:00Z");
  public ZoneId getZone(){return ZoneOffset.UTC;}public Clock withZone(ZoneId zone){return this;}public Instant instant(){return now;}
 }
}
