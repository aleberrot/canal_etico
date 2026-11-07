package com.pantteon.canal_etico.service;

import com.pantteon.canal_etico.exception.FileStorageException;
import com.pantteon.canal_etico.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "r2")
public class R2FileStorageService implements FileStorageService {

    private final S3Client s3Client;
    private final StorageKeyGenerator storageKeyGenerator;
    private final String bucket;

    public R2FileStorageService(
            S3Client s3Client,
            StorageKeyGenerator storageKeyGenerator,
            @Value("${app.storage.r2.bucket}") String bucket
    ) {
        this.s3Client = s3Client;
        this.storageKeyGenerator = storageKeyGenerator;
        this.bucket = bucket;
    }

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
        PutObjectRequest.Builder request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey);
        if (archivo.getContentType() != null && !archivo.getContentType().isBlank()) {
            request.contentType(archivo.getContentType());
        }

        try {
            s3Client.putObject(
                    request.build(),
                    RequestBody.fromBytes(archivo.getBytes())
            );
            return objectKey;
        } catch (IOException | S3Exception exception) {
            throw new FileStorageException("No fue posible guardar el archivo en R2", exception);
        }
    }

    @Override
    public Resource cargarArchivo(String objectKey) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

        try {
            ResponseBytes<GetObjectResponse> objeto = s3Client.getObjectAsBytes(request);
            String filename = storageKeyGenerator.obtenerNombreDescarga(objectKey);
            return new ByteArrayResource(objeto.asByteArray()) {
                @Override
                public String getFilename() {
                    return filename;
                }
            };
        } catch (NoSuchKeyException exception) {
            throw new ResourceNotFoundException("Archivo no encontrado");
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                throw new ResourceNotFoundException("Archivo no encontrado");
            }
            throw new FileStorageException("No fue posible cargar el archivo desde R2", exception);
        }
    }
}
