package com.pantteon.canal_etico.service;


import com.pantteon.canal_etico.dto.CrearDenunciaRequest;
import com.pantteon.canal_etico.dto.CrearDenunciaResponse;
import com.pantteon.canal_etico.exception.DenunciaNotFoundException;
import com.pantteon.canal_etico.model.Denuncia;
import com.pantteon.canal_etico.model.EstadoDenuncia;
import com.pantteon.canal_etico.repository.DenunciaRepository;
import com.pantteon.canal_etico.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DenunciaService {
    private final FileStorageService fileStorageService;
    private final DenunciaRepository denunciaRepository;
    private final PasswordEncoder passwordEncoder;

    public CrearDenunciaResponse crearDenuncia(
            CrearDenunciaRequest request,
            MultipartFile archivo
    ){

        String archivoPath = null;

        if (archivo != null && !archivo.isEmpty()) {
            archivoPath = fileStorageService.guardarArchivo(archivo);
        }

        Denuncia denuncia = new Denuncia();

        String hashedPassword = passwordEncoder.encode(request.password());

        denuncia.setPasswordHash(hashedPassword);
        denuncia.setCodigoUnico(generarCodigoUnico());
        denuncia.setTipo(request.tipo());
        denuncia.setDescripcion(request.descripcion());
        denuncia.setEstado(EstadoDenuncia.valueOf(String.valueOf(EstadoDenuncia.RECIBIDA)));
        denuncia.setArchivoPath(archivoPath);

        denunciaRepository.save(denuncia);

        return mapToResponse(denuncia);
    }

    private String generarCodigoUnico(){

        String codigo;

        do {
            codigo = CodeGenerator.generarCodigo();
        } while (denunciaRepository.findByCodigoUnico(codigo).isPresent());

        return codigo;
    }

    /*
    private String  generarCodigoUnico(){
        String caracteres = "ABCDEFGHIJKLMNOPRSTUVWXYZ0123456789";

        Random random = new Random();
        StringBuilder codigo = new StringBuilder();

        for (int i = 0; i < 8; i++) {
            codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }

        return codigo.toString();
    }
    */
    public CrearDenunciaResponse consultarDenuncia(String codigo, String password) {
        var denuncia = denunciaRepository
                .findByCodigoUnico(codigo)
                .orElseThrow(()-> new RuntimeException("Denuncia no encontrada"));

        boolean passwordValid = passwordEncoder.matches(password, denuncia.getPasswordHash());

        if(!passwordValid){
            throw new RuntimeException("Credenciales Incorrectas");
        }

        return new CrearDenunciaResponse(denuncia);
    }

    public Resource descargarArchivo(String codigo) {

        Denuncia denuncia = denunciaRepository.findByCodigoUnico(codigo)
                .orElseThrow(() -> new RuntimeException("Denuncia no encontrada"));

        try {

            Path rutaArchivo = Paths.get(denuncia.getArchivoPath());
            Resource resource = new UrlResource(rutaArchivo.toUri());

            if (!resource.exists()) {
                throw new RuntimeException("Archivo no encontrado");
            }

            return resource;

        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al leer archivo", e);
        }
    }

    public CrearDenunciaResponse cambiarEstado(UUID id, EstadoDenuncia estado) {
        var denuncia = denunciaRepository.findById(id)
                .orElseThrow(() ->
                        new DenunciaNotFoundException("Denuncia no encontrada")
                );

        denuncia.setEstado(EstadoDenuncia.valueOf(String.valueOf(estado)));

        denunciaRepository.save(denuncia);

        return mapToResponse(denuncia);
    }

    public CrearDenunciaResponse obtenerPorId(UUID id) {
        var denuncia = denunciaRepository.findById(id)
                .orElseThrow(() ->
                        new DenunciaNotFoundException("Denuncia no encontrada")
                );

        return mapToResponse(denuncia);
    }

    public List<CrearDenunciaResponse> listarDenuncias() {
        return denunciaRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private CrearDenunciaResponse mapToResponse(Denuncia denuncia){

        return new CrearDenunciaResponse(
                denuncia.getCodigoUnico(),
                denuncia.getTipo(),
                denuncia.getDescripcion(),
                denuncia.getEstado(),
                denuncia.getFechaIncidente()
        );
    }
}
