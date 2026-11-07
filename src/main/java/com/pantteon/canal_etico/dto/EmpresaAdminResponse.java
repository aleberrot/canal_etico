package com.pantteon.canal_etico.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/** Información de empresa disponible únicamente para administración. */
public record EmpresaAdminResponse(
        @Schema(example = "emp-01") String codigo,
        String nombre,
        String logo,
        String banner,
        String correoNotificacion,
        Boolean activa,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
