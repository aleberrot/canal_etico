package com.pantteon.canal_etico.dto;

public record ErrorResponse(
        int status,
        String message
) {}