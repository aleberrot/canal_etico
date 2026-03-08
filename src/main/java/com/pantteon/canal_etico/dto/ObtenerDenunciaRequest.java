package com.pantteon.canal_etico.dto;

import java.time.LocalDate;

public record ObtenerDenunciaRequest(
        String tipo,
        String descripcion,
        String personasInvolucradas,
        LocalDate fechaIncidente

) {
}
