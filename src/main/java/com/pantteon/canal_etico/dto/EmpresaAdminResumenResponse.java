package com.pantteon.canal_etico.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Resumen de empresa para listados administrativos. */
public record EmpresaAdminResumenResponse(
        @Schema(example = "emp-01") String codigo,
        String nombre,
        Boolean activa
) {
}
