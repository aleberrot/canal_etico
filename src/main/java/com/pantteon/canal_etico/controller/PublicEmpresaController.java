package com.pantteon.canal_etico.controller;

import com.pantteon.canal_etico.dto.EmpresaPublicResponse;
import com.pantteon.canal_etico.service.EmpresaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Consulta pública del branding de una empresa. */
@RestController
@RequestMapping("/api/empresas")
@RequiredArgsConstructor
@Tag(name = "Empresas públicas", description = "Branding público de empresas")
public class PublicEmpresaController {
    private final EmpresaService empresaService;

    @GetMapping("/{codigo}")
    @Operation(summary = "Obtener branding público de una empresa")
    public ResponseEntity<EmpresaPublicResponse> obtener(
            @Parameter(description = "Código de empresa", example = "emp-01") @PathVariable String codigo) {
        return ResponseEntity.ok(empresaService.obtenerEmpresaPublica(codigo));
    }
}
