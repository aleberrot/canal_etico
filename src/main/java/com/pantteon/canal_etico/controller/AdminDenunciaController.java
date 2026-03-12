package com.pantteon.canal_etico.controller;

import com.pantteon.canal_etico.dto.CrearDenunciaResponse;
import com.pantteon.canal_etico.model.EstadoDenuncia;
import com.pantteon.canal_etico.service.DenunciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/denuncias")
@RequiredArgsConstructor
public class AdminDenunciaController {

    private final DenunciaService denunciaService;

    @GetMapping
    public List<CrearDenunciaResponse> listarDenuncias() {
        return denunciaService.listarDenuncias();
    }

    @GetMapping("/{id}")
    public CrearDenunciaResponse obtenerDenuncia(@PathVariable UUID id) {
        return denunciaService.obtenerPorId(id);
    }

    @PatchMapping("/{id}/estado")
    public CrearDenunciaResponse cambiarEstado(
            @PathVariable UUID id,
            @RequestParam EstadoDenuncia estado
    ) {
        return denunciaService.cambiarEstado(id, estado);
    }

    @GetMapping("/{id}/archivo")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable UUID id){

        Resource file = denunciaService.descargarArchivo(String.valueOf(id));

        return ResponseEntity.ok(file);
    }
}