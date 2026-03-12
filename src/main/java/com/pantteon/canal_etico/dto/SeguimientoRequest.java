package com.pantteon.canal_etico.dto;

import jakarta.validation.constraints.NotBlank;

public record SeguimientoRequest(
        @NotBlank(message = "No puede estar el código vacío")
        String codigo,

        @NotBlank(message = "No puede esta la contraseña vacía")
        String password) {

}

