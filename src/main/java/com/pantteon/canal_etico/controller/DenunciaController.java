package com.pantteon.canal_etico.controller;


import com.pantteon.canal_etico.dto.CrearDenunciaRequest;
import com.pantteon.canal_etico.dto.CrearDenunciaResponse;
import com.pantteon.canal_etico.dto.ObtenerDenunciaRequest;
import com.pantteon.canal_etico.dto.SeguimientoRequest;
import com.pantteon.canal_etico.model.Denuncia;
import com.pantteon.canal_etico.service.DenunciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("api/denuncias")
@RequiredArgsConstructor
public class DenunciaController {

    private final DenunciaService denunciaService;
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<CrearDenunciaResponse> crear(
            @Valid @RequestPart("data") CrearDenunciaRequest request,
            @RequestPart(value = "archivo", required = false) MultipartFile archivo
    ){

        var response = denunciaService.crearDenuncia(request, archivo);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("seguimiento")
    public  ResponseEntity<CrearDenunciaResponse> seguir(@Valid @RequestBody SeguimientoRequest request){
        var response = denunciaService.consultarDenuncia(
                request.codigo(),
                request.password()
        );

        return ResponseEntity.ok(response);

    }
}
