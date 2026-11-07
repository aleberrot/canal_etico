package com.pantteon.canal_etico.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmpresaRequest(
        @NotBlank(message = "El código de empresa es obligatorio") String codigo,
        @NotBlank(message = "El nombre de empresa es obligatorio") String nombre,
        String logo,
        String banner,
        @NotBlank(message = "El correo de notificación es obligatorio") @Email(message = "El correo de notificación no es válido") String correoNotificacion
) {}
