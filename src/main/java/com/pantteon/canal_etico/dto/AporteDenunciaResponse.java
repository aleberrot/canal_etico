package com.pantteon.canal_etico.dto;

import java.time.Instant;

/** Public metadata only: never includes storage keys or administrative fields. */
public record AporteDenunciaResponse(
        String id, String informacion, String nombreArchivo,
        boolean tieneArchivo, Instant createdAt, boolean legacy
) {}
