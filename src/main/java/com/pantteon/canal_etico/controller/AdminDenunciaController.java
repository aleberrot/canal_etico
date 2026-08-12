package com.pantteon.canal_etico.controller;

import com.pantteon.canal_etico.dto.ActualizarDetalleAdminRequest;
import com.pantteon.canal_etico.dto.DenunciaAdminResponse;
import com.pantteon.canal_etico.model.EstadoDenuncia;
import com.pantteon.canal_etico.service.DenunciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/admin/denuncias")
@RequiredArgsConstructor
@Tag(name = "Administración de denuncias")
public class AdminDenunciaController {

    private final DenunciaService denunciaService;

    @GetMapping
    @Operation(summary = "Listar todas las denuncias")
    public List<DenunciaAdminResponse> listarDenuncias() {
        return denunciaService.listarDenuncias();
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Obtener denuncia por código único")
    public DenunciaAdminResponse obtenerDenuncia(
            @Parameter(description = "Código único de denuncia", example = "DEN-8AF2KQ") @PathVariable String codigo) {
        return denunciaService.obtenerPorCodigoUnico(codigo);
    }

    @PatchMapping("/{codigo}/estado")
    @Operation(summary = "Cambiar estado de denuncia por código único")
    public DenunciaAdminResponse cambiarEstado(
            @Parameter(description = "Código único de denuncia", example = "DEN-8AF2KQ") @PathVariable String codigo,
            @RequestParam EstadoDenuncia estado
    ) {
        return denunciaService.cambiarEstado(codigo, estado);
    }

    @GetMapping("/{codigo}/archivo")
    @Operation(summary = "Descargar archivo de una denuncia por código único")
    public ResponseEntity<Resource> descargarArchivo(
            @Parameter(description = "Código único de denuncia", example = "DEN-8AF2KQ") @PathVariable String codigo){

        Resource file = denunciaService.descargarArchivo(codigo);

        String filename = file.getFilename() == null
                ? "denuncia-" + codigo
                : file.getFilename();
        MediaType contentType = MediaTypeFactory.getMediaType(filename)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        String contentDisposition = ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build()
                .toString();

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(file);
    }

    @PatchMapping("/{codigo}/detalle")
    public ResponseEntity<DenunciaAdminResponse> actualizarDetalle(
            @PathVariable String codigo,
            @Valid @RequestBody ActualizarDetalleAdminRequest request
            ){
        return ResponseEntity.ok(
                denunciaService.actualizarDetalleAdmin(codigo, request)
        );
    }
}
