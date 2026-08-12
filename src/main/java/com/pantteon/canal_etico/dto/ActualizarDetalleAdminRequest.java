package com.pantteon.canal_etico.dto;

import java.time.LocalDate;

public record ActualizarDetalleAdminRequest (
    String nombreDenunciado,
    LocalDate fechaDescargo,
    String descargo,
    String conclusionesMedidas
){

}