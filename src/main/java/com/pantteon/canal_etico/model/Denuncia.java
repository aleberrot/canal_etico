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

    private String tipo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(columnDefinition = "TEXT")
    private String personasInvolucradas;

    @Column
    private LocalDate fechaIncidente;

    @Column
    private String archivoPath;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private String estado;

    @Column(columnDefinition = "TEXT")
    private String respuestaAdmin;

    private LocalDate createdAt;
    private LocalDate updatedAt;
}
