package com.pantteon.canal_etico.service;
import com.pantteon.canal_etico.model.NotificacionDenuncia.Estado;
import com.pantteon.canal_etico.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Slf4j
public class NotificationDispatcher {
 private final NotificacionRepository repository;
 private final EmailService email;
 private final BusinessTime time;
 private final java.util.concurrent.atomic.AtomicReference<String> configurationProblem=new java.util.concurrent.atomic.AtomicReference<>();
 @Transactional
 public void procesarPendientes(){
  if(!email.configurada()){
   String problem=email.problemaConfiguracion();
   if(problem==null)problem="NOTIFICATION_CONFIGURATION_INCOMPLETE";
   if(!problem.equals(configurationProblem.getAndSet(problem)))log.warn("Notification delivery paused: {}",problem);
   return; // Retain pending events until configuration is complete.
  }
  if(configurationProblem.getAndSet(null)!=null)log.info("Notification delivery configuration restored");
  for(var n:repository.pendientes(Estado.PENDIENTE,time.now(),PageRequest.of(0,10))){
   n.setIntentos(n.getIntentos()+1);
   try{
    email.enviar(n);n.setEstado(Estado.ENVIADA);n.setEnviadoAt(time.now());n.setUltimoError(null);
    log.info("Notification event {} accepted by mail transport",n.getId());
   }catch(Exception error){
    n.setUltimoError(codigoError(error));
    n.setProximoIntento(time.now().plusSeconds(Math.min(3600,60L*(1L<<Math.min(n.getIntentos()-1,6)))));
    if(n.getIntentos()>=5)n.setEstado(Estado.FALLIDA);
    log.warn("Notification event {} failed on attempt {}: {}",n.getId(),n.getIntentos(),n.getUltimoError());
   }
   repository.save(n);
  }
 }
 private static String codigoError(Throwable error){
  var pending=new java.util.ArrayDeque<Throwable>();pending.add(error);
  var visited=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<Throwable,Boolean>());
  while(!pending.isEmpty()&&visited.size()<32){
   var current=pending.removeFirst();if(!visited.add(current))continue;
   if(current instanceof com.pantteon.canal_etico.infra.GmailApiMailSender.DeliveryException gmail)return gmail.code();
   if(current instanceof org.springframework.mail.MailAuthenticationException || current instanceof jakarta.mail.AuthenticationFailedException)return "SMTP_AUTHENTICATION_FAILED";
   if(current instanceof java.net.SocketTimeoutException)return "SMTP_TIMEOUT";
   if(current instanceof java.net.UnknownHostException)return "SMTP_DNS_FAILED";
   if(current instanceof java.net.ConnectException)return "SMTP_CONNECTION_FAILED";
   if(current instanceof javax.net.ssl.SSLException)return "SMTP_TLS_FAILED";
   if(current instanceof jakarta.mail.SendFailedException)return "SMTP_REJECTED";
   if(current.getCause()!=null)pending.add(current.getCause());
   if(current instanceof jakarta.mail.MessagingException mail && mail.getNextException()!=null)pending.add(mail.getNextException());
   if(current instanceof org.springframework.mail.MailSendException mail)pending.addAll(mail.getFailedMessages().values());
  }
  return "SMTP_DELIVERY_FAILED";
 }
}

