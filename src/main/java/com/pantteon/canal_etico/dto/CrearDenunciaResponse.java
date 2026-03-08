package com.pantteon.canal_etico.dto;

import com.pantteon.canal_etico.model.Denuncia;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CrearDenunciaResponse(
        String codigo,
        String tipo,
        String descripcion,
        String estado,
        LocalDate fechaCreacion
) {
    public CrearDenunciaResponse(Denuncia denuncia) {
        this(
                denuncia.getCodigoUnico(),
                denuncia.getTipo(),
                denuncia.getDescripcion(),
                denuncia.getEstado(),
                denuncia.getFechaIncidente()
        );
    }
}
