package com.pantteon.canal_etico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CrearDenunciaRequest(
        @NotBlank(message = "Tipo de denuncia obligatorio")
        String tipo,

        @NotBlank(message = "La descripción no puede estar vacía")
        @Size(min = 20, message = "La descripción debe tener al menos 20 caracteres")
        String descripcion,

        @Size(max = 200, message = "Personas involucradas no puede superar 200 caracteres")
        String PersonasInvolucradas,


        LocalDate fechaIncidente,

        @NotBlank(message = "Debe establecer una contraseña para seguimiento")
        @Size(min = 6, message = "La contraseña debe tener mínimo 6 caracteres")
        String password
) {
}
