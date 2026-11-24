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
   var bytes=new java.io.ByteArrayOutputStream();m.writeTo(bytes);String raw=bytes.toString(java.nio.charset.StandardCharsets.UTF_8);
   assertTrue(raw.contains("multipart/alternative"));assertTrue(raw.contains("text/plain"));assertTrue(raw.contains("text/html"));
   assertFalse(raw.contains("Content-Disposition: attachment"));assertTrue(raw.contains("X-WEBETIKO-Event-ID"));
  }
 }
 @Test void missingProviderOrRecipientsDoNotPretendSuccess(){
  @SuppressWarnings("unchecked") ObjectProvider<JavaMailSender> provider=mock(ObjectProvider.class);
  assertFalse(new EmailService(provider,new BusinessTime(Clock.systemUTC()),"","https://example.invalid/admin","").configurada());
 }
}
