package com.pantteon.canal_etico.controller;

import com.pantteon.canal_etico.dto.DenunciaAdminResponse;
import com.pantteon.canal_etico.dto.EstadisticasEmpresaResponse;
import com.pantteon.canal_etico.service.DenunciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/empresas/{codigoEmpresa}")
@RequiredArgsConstructor
@Tag(name = "Denuncias por empresa")
public class AdminEmpresaDenunciaController {
    private final DenunciaService denunciaService;

    @GetMapping("/denuncias")
    @Operation(summary = "Listar denuncias de una empresa")
    public ResponseEntity<List<DenunciaAdminResponse>> listar(
            @Parameter(description = "Código de empresa", example = "emp-01") @PathVariable String codigoEmpresa) {
        return ResponseEntity.ok(denunciaService.listarDenunciasPorEmpresa(codigoEmpresa));
    }

    @GetMapping("/estadisticas")
    @Operation(summary = "Obtener estadísticas de una empresa")
    public ResponseEntity<EstadisticasEmpresaResponse> estadisticas(
            @Parameter(description = "Código de empresa", example = "emp-01") @PathVariable String codigoEmpresa) {
        return ResponseEntity.ok(denunciaService.obtenerEstadisticasEmpresa(codigoEmpresa));
    }
}
