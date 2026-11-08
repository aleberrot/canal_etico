package com.pantteon.canal_etico.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "aportes_denuncia", indexes = @Index(name = "idx_aportes_denuncia_fecha", columnList = "denuncia_id,created_at"))
@Getter
@Setter
public class AporteDenuncia {
    @Id
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "denuncia_id", nullable = false, updatable = false)
    private Denuncia denuncia;

    @Column(columnDefinition = "TEXT", updatable = false)
    private String informacion;

    @Column(updatable = false)
    private String archivoObjectKey;

    @Column(updatable = false)
    private String nombreArchivo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
