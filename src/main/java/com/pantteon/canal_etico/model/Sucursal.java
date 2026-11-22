package com.pantteon.canal_etico.model;
import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;
@Entity @Table(name="sucursales", uniqueConstraints=@UniqueConstraint(columnNames={"empresa_id","codigo"})) @Data
public class Sucursal {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="empresa_id",nullable=false) private Empresa empresa;
 @Column(nullable=false,length=80) private String codigo;
 @Column(nullable=false,length=200) private String nombre;
 @Column(nullable=false) private boolean activa=true;
}
