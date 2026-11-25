package com.pantteon.canal_etico;
import com.pantteon.canal_etico.model.*;
import com.pantteon.canal_etico.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Session;
import org.springframework.mail.javamail.JavaMailSender;
import java.time.*;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class EmailNotificationTest {
 @Test void exactFourSubjectsRecipientsAndSafeMetadata() throws Exception {
  JavaMailSender sender=mock(JavaMailSender.class);
  @SuppressWarnings("unchecked") ObjectProvider<JavaMailSender> provider=mock(ObjectProvider.class);
  when(provider.getIfAvailable()).thenReturn(sender);when(provider.getObject()).thenReturn(sender);
  when(sender.createMimeMessage()).thenAnswer(i->new MimeMessage(Session.getInstance(new Properties())));
  var time=new BusinessTime(Clock.systemUTC());
  var email=new EmailService(provider,time,"one@example.invalid,two@example.invalid,three@example.invalid,ONE@example.invalid","https://example.invalid/admin","notifications@example.invalid");
  for(var tipo:TipoDenuncia.values())for(boolean aporte:new boolean[]{false,true}){
   var n=new NotificacionDenuncia();n.setEmpresa("Empresa sintética");n.setSucursal("Sucursal sintética");n.setCodigo("DEN-SYNTHETIC");n.setTipo(tipo);n.setAporte(aporte);n.setOcurridoAt(Instant.parse("2026-11-24T00:30:00Z"));
   email.enviar(n);
  }
  var messages=org.mockito.ArgumentCaptor.forClass(MimeMessage.class);verify(sender,times(4)).send(messages.capture());
  var subjects=new HashSet<String>();for(var m:messages.getAllValues())subjects.add(m.getSubject());
  assertEquals(Set.of("Detalle de la Denuncia General","Detalle de la Denuncia Karin","Nuevos antecedentes agregados a una Denuncia General","Nuevos antecedentes agregados a una Denuncia Karin"),subjects);
  for(var m:messages.getAllValues()){
   assertEquals(3,m.getRecipients(jakarta.mail.Message.RecipientType.TO).length);
   assertEquals(List.of("one@example.invalid","two@example.invalid","three@example.invalid"),Arrays.stream(m.getRecipients(jakarta.mail.Message.RecipientType.TO)).map(a->((jakarta.mail.internet.InternetAddress)a).getAddress()).toList());
   String plain=body(m,"text/plain"),html=body(m,"text/html");
   assertTrue(plain.contains("23/11/2026 21:30:00"));assertTrue(html.contains("23/11/2026 21:30:00"));
   assertTrue(plain.contains("https://example.invalid/admin"));assertTrue(html.contains("https://example.invalid/admin"));
   var bytes=new java.io.ByteArrayOutputStream();m.writeTo(bytes);String raw=bytes.toString(java.nio.charset.StandardCharsets.UTF_8);
   assertTrue(raw.contains("multipart/alternative"));assertTrue(raw.contains("text/plain"));assertTrue(raw.contains("text/html"));
   assertFalse(raw.contains("Content-Disposition: attachment"));assertTrue(raw.contains("X-WEBETIKO-Event-ID"));
  }
 }
 private String body(jakarta.mail.Part part,String type) throws Exception {
  if(part.isMimeType(type))return (String)part.getContent();
  if(part.getContent() instanceof jakarta.mail.Multipart multi){
   var text=new StringBuilder();for(int i=0;i<multi.getCount();i++)text.append(body(multi.getBodyPart(i),type));return text.toString();
  }
  return "";
 }
 @Test void exampleConfigurationHasThreeUniqueMailboxes() throws Exception {
  var lines=java.nio.file.Files.readAllLines(java.nio.file.Path.of(".env.example"));
  var configured=lines.stream().filter(s->s.startsWith("NOTIFICATION_RECIPIENTS=")).findFirst().orElseThrow().split("=",2)[1];
  var recipients=Arrays.stream(configured.split(",")).map(String::trim).map(s->s.toLowerCase(Locale.ROOT)).toList();
  assertEquals(3,recipients.size());assertEquals(3,new HashSet<>(recipients).size());
  for(String recipient:recipients)new jakarta.mail.internet.InternetAddress(recipient,true).validate();
 }
 @Test void missingProviderOrRecipientsDoNotPretendSuccess(){
  @SuppressWarnings("unchecked") ObjectProvider<JavaMailSender> provider=mock(ObjectProvider.class);
  assertFalse(new EmailService(provider,new BusinessTime(Clock.systemUTC()),"","https://example.invalid/admin","").configurada());
 }
 @Test void blankFromUsesAuthenticatedMailboxInsteadOfMachineIdentity() throws Exception {
  var sender=spy(new org.springframework.mail.javamail.JavaMailSenderImpl());
  sender.setHost("smtp.example.invalid");sender.setUsername("sender@example.invalid");
  doNothing().when(sender).send(any(MimeMessage.class));
  @SuppressWarnings("unchecked") ObjectProvider<JavaMailSender> provider=mock(ObjectProvider.class);
  when(provider.getIfAvailable()).thenReturn(sender);when(provider.getObject()).thenReturn(sender);
  var email=new EmailService(provider,new BusinessTime(Clock.systemUTC()),"recipient@example.invalid","https://example.invalid/admin","");
  var n=new NotificacionDenuncia();n.setTipo(TipoDenuncia.GENERAL);n.setEmpresa("Empresa sintética");n.setCodigo("DEN-SYNTHETIC");n.setOcurridoAt(Instant.parse("2026-11-24T00:30:00Z"));
  email.enviar(n);
  var captured=org.mockito.ArgumentCaptor.forClass(MimeMessage.class);verify(sender).send(captured.capture());
  assertNotNull(captured.getValue().getFrom());
  assertEquals("sender@example.invalid",((jakarta.mail.internet.InternetAddress)captured.getValue().getFrom()[0]).getAddress());
 }
 @Test void missingMailboxRetainsEventsUntilSenderIsConfigured(){
  var sender=new org.springframework.mail.javamail.JavaMailSenderImpl();sender.setHost("smtp.example.invalid");sender.setUsername("relay-user");
  @SuppressWarnings("unchecked") ObjectProvider<JavaMailSender> provider=mock(ObjectProvider.class);
  when(provider.getIfAvailable()).thenReturn(sender);
  assertFalse(new EmailService(provider,new BusinessTime(Clock.systemUTC()),"recipient@example.invalid","https://example.invalid/admin","").configurada());
 }
}
