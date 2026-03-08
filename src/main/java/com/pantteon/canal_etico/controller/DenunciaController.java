package com.pantteon.canal_etico.controller;


import com.pantteon.canal_etico.dto.CrearDenunciaRequest;
import com.pantteon.canal_etico.dto.CrearDenunciaResponse;
import com.pantteon.canal_etico.dto.ObtenerDenunciaRequest;
import com.pantteon.canal_etico.model.Denuncia;
import com.pantteon.canal_etico.service.DenunciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/denuncias")
@RequiredArgsConstructor
public class DenunciaController {

    private final DenunciaService denunciaService;
    @PostMapping
    public ResponseEntity<CrearDenunciaResponse> crear(@RequestBody CrearDenunciaRequest request){
        var denuncia = denunciaService.crearDenuncia(request);
        return new ResponseEntity<>(denuncia,HttpStatus.CREATED);
    }

    
}
