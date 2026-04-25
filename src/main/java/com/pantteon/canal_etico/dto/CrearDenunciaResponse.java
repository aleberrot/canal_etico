package com.pantteon.canal_etico.dto;

import com.pantteon.canal_etico.model.Denuncia;
import com.pantteon.canal_etico.model.EstadoDenuncia;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CrearDenunciaResponse(
        String codigo,
        String tipo,
        String descripcion,
        EstadoDenuncia estado,
        LocalDate fechaCreacion
) {
    public CrearDenunciaResponse(Denuncia denuncia) {
        this(
                denuncia.getCodigoUnico(),
                denuncia.getTipo(),
                denuncia.getDescripcion(),
                EstadoDenuncia.valueOf(denuncia.getEstado().name()),
                denuncia.getFechaIncidente()
        );
    }
}
