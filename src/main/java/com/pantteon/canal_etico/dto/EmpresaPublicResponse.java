package com.pantteon.canal_etico.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Branding que puede consultarse sin autenticación. */
public record EmpresaPublicResponse(
        @Schema(example = "emp-01") String codigo,
        String nombre,
        String logo,
        String banner,
        boolean activa
) {
}
