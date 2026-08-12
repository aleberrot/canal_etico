package com.pantteon.canal_etico.controller;


import com.pantteon.canal_etico.dto.CrearDenunciaRequest;
import com.pantteon.canal_etico.dto.CrearDenunciaResponse;
import com.pantteon.canal_etico.dto.SeguimientoDenunciaResponse;
import com.pantteon.canal_etico.dto.SeguimientoRequest;
import com.pantteon.canal_etico.service.DenunciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("api/denuncias")
@RequiredArgsConstructor
@Tag(name = "Denuncias", description = "Canal público de denuncias")
public class DenunciaController {

    private final DenunciaService denunciaService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear una denuncia para una empresa")
    public ResponseEntity<CrearDenunciaResponse> crear(
            @Valid @RequestPart("data") CrearDenunciaRequest request,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo
    ){

        var response = denunciaService.crearDenuncia(request, archivo);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @PostMapping("seguimiento")
    @Operation(summary = "Consultar el seguimiento de una denuncia")
    public ResponseEntity<SeguimientoDenunciaResponse> seguir(@Valid @RequestBody SeguimientoRequest request){
        var response = denunciaService.consultarDenuncia(
                request.codigo(),
                request.password()
        );

        return ResponseEntity.ok(response);

    }

    @PostMapping(value = "/{codigo}/informacion-adicional", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Agregar información adicional a una denuncia general")
    public ResponseEntity<SeguimientoDenunciaResponse> agregarInformacionAdicional(
            @PathVariable String codigo,
            @RequestPart("password") String password,
            @RequestPart(value = "informacion", required = false) String informacion,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo) {
        return ResponseEntity.ok(denunciaService.agregarInformacionAdicional(codigo, password, informacion, archivo));
    }
}
