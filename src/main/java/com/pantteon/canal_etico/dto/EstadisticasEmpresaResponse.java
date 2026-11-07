package com.pantteon.canal_etico.dto;

public record EstadisticasEmpresaResponse(
        long total,
        long generales,
        long leyKarin,
        long recibidas,
        long enRevision,
        long cerradas
) {}
