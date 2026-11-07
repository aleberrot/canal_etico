package com.pantteon.canal_etico.dto;

import com.pantteon.canal_etico.model.EstadoDenuncia;
import com.pantteon.canal_etico.model.RelacionEmpresa;
import com.pantteon.canal_etico.model.TipoDenuncia;

import java.time.LocalDate;

/** Información completa de una denuncia para el panel administrativo. */
public record DenunciaAdminResponse(
        String codigoUnico,
        TipoDenuncia tipoDenuncia,
        String delitoCodigo,
        String delitoTipificacion,
        String delitoDescripcion,
        RelacionEmpresa relacionEmpresa,
        String descripcion,
        String personasInvolucradas,
        LocalDate fechaIncidente,
        EstadoDenuncia estado,
        String nombreDenunciante,
        String emailDenunciante,
        String telefonoDenunciante,
        String testigos,
        String nombreDenunciado,
        LocalDate fechaDescargo,
        String descargo,
        String conclusionesMedidas,
        String informacionAdicional,
        LocalDate createdAt,
        LocalDate updatedAt
) {}
