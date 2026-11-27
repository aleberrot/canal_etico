package com.pantteon.canal_etico;
import com.pantteon.canal_etico.model.NotificacionDenuncia;
import com.pantteon.canal_etico.repository.NotificacionRepository;
import com.pantteon.canal_etico.service.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class NotificationDispatcherTest {
 NotificacionRepository repository=mock(NotificacionRepository.class);
 EmailService email=mock(EmailService.class);
 BusinessTime time=new BusinessTime(Clock.fixed(Instant.parse("2026-11-24T00:30:00Z"),ZoneOffset.UTC));
 NotificationDispatcher dispatcher=new NotificationDispatcher(repository,email,time);
 @Test void smtpFailureRemainsRetryableAndDoesNotTouchReports() throws Exception {
  var n=new NotificacionDenuncia();when(email.configurada()).thenReturn(true);
  when(repository.pendientes(any(),any(),any())).thenReturn(List.of(n));
  doThrow(new IllegalStateException("synthetic SMTP failure")).when(email).enviar(n);
  dispatcher.procesarPendientes();assertEquals(NotificacionDenuncia.Estado.PENDIENTE,n.getEstado());
  assertEquals(1,n.getIntentos());assertEquals(time.now().plusSeconds(60),n.getProximoIntento());
  assertEquals("SMTP_DELIVERY_FAILED",n.getUltimoError());assertNull(n.getEnviadoAt());
  reset(email);when(email.configurada()).thenReturn(true);dispatcher.procesarPendientes();
  assertEquals(NotificacionDenuncia.Estado.ENVIADA,n.getEstado());assertEquals(2,n.getIntentos());assertNull(n.getUltimoError());
 }
 @Test void unavailableConfigurationRetainsOutboxWithoutPretendingDelivery(){
  dispatcher.procesarPendientes();verifyNoInteractions(repository);
 }
 @Test void fifthFailureStopsAutomaticRetries() throws Exception {
  var n=new NotificacionDenuncia();n.setIntentos(4);when(email.configurada()).thenReturn(true);
  when(repository.pendientes(any(),any(),any())).thenReturn(List.of(n));
  doThrow(new IllegalStateException("synthetic")).when(email).enviar(n);
  dispatcher.procesarPendientes();assertEquals(NotificacionDenuncia.Estado.FALLIDA,n.getEstado());
 }
 @Test void transportFailuresAreClassifiedWithoutLeakingProviderMessages() throws Exception {
  var cases=Map.<Exception,String>of(
   new org.springframework.mail.MailAuthenticationException("synthetic-secret"),"SMTP_AUTHENTICATION_FAILED",
   new org.springframework.mail.MailSendException("synthetic-secret",new jakarta.mail.MessagingException("private provider text",new java.net.SocketTimeoutException("synthetic-secret"))),"SMTP_TIMEOUT",
   new org.springframework.mail.MailSendException("synthetic",new java.net.UnknownHostException("private-host")),"SMTP_DNS_FAILED",
   new org.springframework.mail.MailSendException("synthetic",new javax.net.ssl.SSLHandshakeException("private-host")),"SMTP_TLS_FAILED",
   new org.springframework.mail.MailSendException("synthetic",new java.net.ConnectException("private-host")),"SMTP_CONNECTION_FAILED");
  for(var entry:cases.entrySet()){
   var n=new NotificacionDenuncia();reset(email);when(email.configurada()).thenReturn(true);
   when(repository.pendientes(any(),any(),any())).thenReturn(List.of(n));
   doThrow(entry.getKey()).when(email).enviar(n);
   dispatcher.procesarPendientes();assertEquals(entry.getValue(),n.getUltimoError());
   assertEquals(NotificacionDenuncia.Estado.PENDIENTE,n.getEstado());
  }
 }
}
