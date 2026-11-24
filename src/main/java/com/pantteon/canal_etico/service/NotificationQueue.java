package com.pantteon.canal_etico.service;
import com.pantteon.canal_etico.model.*;
import com.pantteon.canal_etico.repository.NotificacionRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
@Service @RequiredArgsConstructor
public class NotificationQueue {
 private final NotificacionRepository repository;
 @Transactional(propagation=Propagation.MANDATORY)
 public void nueva(Denuncia denuncia,UUID aporteId,Instant ocurridoAt){
  String evento=aporteId==null?"NUEVA:"+denuncia.getId():"APORTE:"+aporteId;
  if(repository.existsByEvento(evento))return;
  var n=new NotificacionDenuncia();n.setEvento(evento);n.setEmpresa(denuncia.getEmpresa().getNombre());
  n.setSucursal(denuncia.getSucursalNombre());n.setCodigo(denuncia.getCodigoUnico());
  n.setTipo(denuncia.getTipoDenuncia());n.setAporte(aporteId!=null);
  n.setOcurridoAt(ocurridoAt);n.setProximoIntento(ocurridoAt);repository.save(n);
 }
}

