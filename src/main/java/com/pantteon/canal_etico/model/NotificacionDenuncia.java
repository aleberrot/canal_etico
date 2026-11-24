package com.pantteon.canal_etico.model;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
/** Transactional notification metadata only. Never store complaint contents here. */
@Entity @Table(name="notificaciones_denuncia",indexes=@Index(name="idx_notificacion_pendiente",columnList="estado,proximo_intento"))
@Getter @Setter
public class NotificacionDenuncia {
 public enum Estado { PENDIENTE, ENVIADA, FALLIDA }
 @Id private UUID id=UUID.randomUUID();
 @Column(nullable=false,unique=true,updatable=false,length=100) private String evento;
 @Column(nullable=false,updatable=false) private String empresa;
 @Column(updatable=false) private String sucursal;
 @Column(nullable=false,updatable=false) private String codigo;
 @Enumerated(EnumType.STRING) @Column(nullable=false,updatable=false) private TipoDenuncia tipo;
 @Column(nullable=false,updatable=false) private boolean aporte;
 @Column(nullable=false,updatable=false) private Instant ocurridoAt;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private Estado estado=Estado.PENDIENTE;
 private int intentos;
 @Column(name="proximo_intento",nullable=false) private Instant proximoIntento;
 private Instant enviadoAt;
 // Sanitized category; provider exceptions may contain recipient/credential data.
 private String ultimoError;
}

