package com.pantteon.canal_etico.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record SeguimientoRequest(
        @NotBlank(message = "No puede estar el código vacío")
        @JsonAlias("code")
        String codigo,

        @NotBlank(message = "No puede esta la contraseña vacía")
        String password) {

}

