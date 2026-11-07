package com.pantteon.canal_etico.service;

import com.pantteon.canal_etico.exception.FileStorageException;
import com.pantteon.canal_etico.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@ConditionalOnProperty(
        name = "app.storage.type",
        havingValue = "local",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class LocalFileStorageService implements FileStorageService {

    private final StorageKeyGenerator storageKeyGenerator;
    private final Path storagePath = Paths.get("uploads").toAbsolutePath().normalize();

    @Override
    public String guardarArchivo(
            MultipartFile archivo,
            String codigoEmpresa,
            String codigoDenuncia
    ) {
        String objectKey = storageKeyGenerator.generar(
                codigoEmpresa,
                codigoDenuncia,
                archivo.getOriginalFilename()
        );
        Path destino = resolverRutaSegura(objectKey);

        try {
            Files.createDirectories(destino.getParent());
            Files.copy(
                    archivo.getInputStream(),
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
            );
            return objectKey;
        } catch (IOException exception) {
            throw new FileStorageException("No fue posible guardar el archivo", exception);
        }
    }

    @Override
    public Resource cargarArchivo(String objectKey) {
        Path archivo = resolverRutaSegura(objectKey);
        if (!Files.exists(archivo) || !Files.isRegularFile(archivo)) {
            throw new ResourceNotFoundException("Archivo no encontrado");
        }

        String filename = storageKeyGenerator.obtenerNombreDescarga(objectKey);
        return new FileSystemResource(archivo) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }

    private Path resolverRutaSegura(String objectKey) {
        Path ruta = storagePath.resolve(objectKey).normalize();
        if (!ruta.startsWith(storagePath)) {
            throw new FileStorageException("La clave de almacenamiento no es válida");
        }
        return ruta;
    }
}
