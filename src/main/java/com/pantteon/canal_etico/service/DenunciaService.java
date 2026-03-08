package com.pantteon.canal_etico.service;


import com.pantteon.canal_etico.dto.CrearDenunciaRequest;
import com.pantteon.canal_etico.dto.CrearDenunciaResponse;
import com.pantteon.canal_etico.model.Denuncia;
import com.pantteon.canal_etico.repository.DenunciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class DenunciaService {
    private final DenunciaRepository denunciaRepository;
    private final PasswordEncoder passwordEncoder;

    public CrearDenunciaResponse crearDenuncia(CrearDenunciaRequest request){
        Denuncia denuncia = new Denuncia();

        denuncia.setTipo(request.tipo());
        denuncia.setDescripcion(request.descripcion());
        denuncia.setPersonasInvolucradas(request.PersonasInvolucradas());
        denuncia.setFechaIncidente(request.fechaIncidente());

        denuncia.setEstado("PENDIENTE");

        denuncia.setPasswordHash(passwordEncoder.encode(request.password()));
        denuncia.setCodigoUnico(generarCodigoUnico());

        var denunciaGuardada = denunciaRepository.save(denuncia);

        return new CrearDenunciaResponse(denunciaGuardada);
    }

    private String  generarCodigoUnico(){
        String caracteres = "ABCDEFGHIJKLMNOPRSTUVWXYZ0123456789";

        Random random = new Random();
        StringBuilder codigo = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }

        return codigo.toString();
    }
}
