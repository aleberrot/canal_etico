package com.pantteon.canal_etico.controller;

import com.pantteon.canal_etico.model.NotificacionDenuncia.Estado;
import com.pantteon.canal_etico.repository.NotificacionRepository;
import com.pantteon.canal_etico.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Existing /api/admin/** security applies. Read only; never sends or retries mail. */
@RestController
@RequestMapping("/api/admin/notificaciones")
public class NotificationStatusController {
 private final NotificacionRepository repository;
 private final EmailService email;
 private final boolean enabled;
 public NotificationStatusController(NotificacionRepository repository,EmailService email,
   @Value("${app.notifications.enabled:true}") boolean enabled){
  this.repository=repository;this.email=email;this.enabled=enabled;
 }
 public record Status(boolean habilitado,boolean configurada,String problemaConfiguracion,int cantidadDestinatarios,
   long pendientes,long aceptadasPorTransporte,long fallidas){}
 @GetMapping("/estado")
 public ResponseEntity<Status> status(){
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Status(
   enabled,email.configurada(),email.problemaConfiguracion(),email.cantidadDestinatarios(),
   repository.countByEstado(Estado.PENDIENTE),repository.countByEstado(Estado.ENVIADA),repository.countByEstado(Estado.FALLIDA)));
 }
}
