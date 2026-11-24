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
 @Transactional
 public void procesarPendientes(){
  if(!email.configurada())return; // Retain pending events until configuration is complete.
  for(var n:repository.pendientes(Estado.PENDIENTE,time.now(),PageRequest.of(0,10))){
   n.setIntentos(n.getIntentos()+1);
   try{
    email.enviar(n);n.setEstado(Estado.ENVIADA);n.setEnviadoAt(time.now());n.setUltimoError(null);
    log.info("Notification event {} delivered",n.getId());
   }catch(Exception error){
    n.setUltimoError("SMTP_DELIVERY_FAILED");
    n.setProximoIntento(time.now().plusSeconds(Math.min(3600,60L*(1L<<Math.min(n.getIntentos()-1,6)))));
    if(n.getIntentos()>=5)n.setEstado(Estado.FALLIDA);
    log.warn("Notification event {} failed on attempt {}",n.getId(),n.getIntentos());
   }
   repository.save(n);
  }
 }
}

