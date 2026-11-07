package com.pantteon.canal_etico.controller;

import com.pantteon.canal_etico.dto.EmpresaRequest;
import com.pantteon.canal_etico.dto.EmpresaAdminResponse;
import com.pantteon.canal_etico.dto.EmpresaAdminResumenResponse;
import com.pantteon.canal_etico.service.EmpresaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/empresas")
@RequiredArgsConstructor
@Tag(name = "Empresas", description = "Administración de empresas de la plataforma")
public class EmpresaController {
    private final EmpresaService empresaService;

    @PostMapping
    @Operation(summary = "Crear empresa")
    public ResponseEntity<EmpresaAdminResponse> crear(@Valid @RequestBody EmpresaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaService.crearEmpresa(request));
    }

    @GetMapping
    @Operation(summary = "Listar empresas")
    public ResponseEntity<List<EmpresaAdminResumenResponse>> listar() {
        return ResponseEntity.ok(empresaService.listarEmpresas());
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Obtener empresa por código")
    public ResponseEntity<EmpresaAdminResponse> obtener(
            @Parameter(description = "Código de empresa", example = "emp-01") @PathVariable String codigo) {
        return ResponseEntity.ok(empresaService.obtenerEmpresa(codigo));
    }

    @PatchMapping("/{codigo}/activar")
    @Operation(summary = "Activar empresa por código")
    public ResponseEntity<EmpresaAdminResponse> activar(
            @Parameter(description = "Código de empresa", example = "emp-01") @PathVariable String codigo) {
        return ResponseEntity.ok(empresaService.activarEmpresa(codigo));
    }

    @PatchMapping("/{codigo}/desactivar")
    @Operation(summary = "Desactivar empresa por código")
    public ResponseEntity<EmpresaAdminResponse> desactivar(
            @Parameter(description = "Código de empresa", example = "emp-01") @PathVariable String codigo) {
        return ResponseEntity.ok(empresaService.desactivarEmpresa(codigo));
    }
}
