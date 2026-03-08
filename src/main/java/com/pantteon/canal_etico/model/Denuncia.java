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

    @Column(unique = true)
    private String codigoUnico;

    private String tipo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(columnDefinition = "TEXT")
    private String personasInvolucradas;

    private LocalDate fechaIncidente;

    private String archivoPath;

    private String passwordHash;

    private String estado;

    @Column(columnDefinition = "TEXT")
    private String respuestaAdmin;

    private LocalDate createdAt;
    private LocalDate updatedAt;
}
