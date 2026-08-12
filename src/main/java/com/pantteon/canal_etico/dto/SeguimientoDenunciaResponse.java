package com.pantteon.canal_etico.dto;

import com.pantteon.canal_etico.model.EstadoDenuncia;
import com.pantteon.canal_etico.model.TipoDenuncia;

import java.time.LocalDate;

public record SeguimientoDenunciaResponse(

        String codigoUnico,

        TipoDenuncia tipoDenuncia,

        String delitoTipificacion,

        EstadoDenuncia estado,

        String descripcion,

        String informacionAdicional,

        String conclusionesMedidas,

        LocalDate createdAt,

        LocalDate updatedAt,

        LocalDate fechaIncidente
) {
}
