package com.pantteon.canal_etico.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "denuncias")
@Data
public class Denuncia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String codigoUnico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDenuncia tipoDenuncia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelacionEmpresa relacionEmpresa;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(columnDefinition = "TEXT")
    private String personasInvolucradas;

    private String nombreDenunciante;

    private String emailDenunciante;

    private String telefonoDenunciante;

    @Column
    private LocalDate fechaIncidente;

    @Column
    private String archivoPath;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDenuncia estado;

    @Column(columnDefinition = "TEXT")
    private String respuestaAdmin;

    private String delitoCodigo;

    private String delitoTipificacion;

    @Column(columnDefinition = "TEXT")
    private String delitoDescripcion;

    private String nombreDenunciado;

    @Column(columnDefinition = "TEXT")
    private String descargo;

    private LocalDate fechaDescargo;

    @Column(columnDefinition = "TEXT")
    private String conclusionesMedidas;

    @Column(columnDefinition = "TEXT")
    private String testigos;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @Column(columnDefinition = "TEXT")
    private String informacionAdicional;

    private String documentoAdicionalPath;

    private LocalDate createdAt;
    private LocalDate updatedAt;
}
