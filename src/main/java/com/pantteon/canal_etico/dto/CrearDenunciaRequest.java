package com.pantteon.canal_etico.dto;

import com.pantteon.canal_etico.model.RelacionEmpresa;
import com.pantteon.canal_etico.model.TipoDenuncia;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CrearDenunciaRequest(
        @NotBlank(message = "El código de empresa es obligatorio")
        String codigoEmpresa,

        @NotNull(message = "Tipo de denuncia obligatorio")
        TipoDenuncia tipoDenuncia,

        String telefonoDenunciante,

        @Email
        String emailDenunciante,

        String nombreDenunciante,

        String testigos,

        @NotBlank
        String delitoCodigo,

        @NotBlank
        String delitoTipificacion,

        String delitoDescripcion,

        @NotNull
        RelacionEmpresa relacionEmpresa,

        @NotBlank(message = "La descripción no puede estar vacía")
        @Size(min = 20, message = "La descripción debe tener al menos 20 caracteres")
        String descripcion,

        @Size(max = 200, message = "Personas involucradas no puede superar 200 caracteres")
        String personasInvolucradas,


        LocalDate fechaIncidente,

        @NotBlank(message = "Debe establecer una contraseña para seguimiento")
        @Size(min = 6, message = "La contraseña debe tener mínimo 6 caracteres")
        String password
) {
}
