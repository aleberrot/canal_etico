package com.pantteon.canal_etico.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.pantteon.canal_etico.model.Denuncia;
import com.pantteon.canal_etico.model.EstadoDenuncia;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record CrearDenunciaResponse(
        @JsonAlias("code")
        String codigo,
        String tipo,
        String descripcion,
        EstadoDenuncia estado,
        LocalDate fechaCreacion
) {
    public CrearDenunciaResponse(Denuncia denuncia) {
        this(
                denuncia.getCodigoUnico(),
                denuncia.getTipoDenuncia().name(),
                denuncia.getDescripcion(),
                EstadoDenuncia.valueOf(denuncia.getEstado().name()),
                denuncia.getFechaIncidente()
        );
    }
}
