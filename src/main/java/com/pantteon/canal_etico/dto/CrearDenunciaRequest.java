package com.pantteon.canal_etico.dto;

import java.time.LocalDate;

public record CrearDenunciaRequest(
        String tipo,
        String descripcion,
        String PersonasInvolucradas,
        LocalDate fechaIncidente,
        String password
) {
}
