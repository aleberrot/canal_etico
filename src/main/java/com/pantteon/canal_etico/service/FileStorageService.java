package com.pantteon.canal_etico.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String guardarArchivo(
            MultipartFile archivo,
            String codigoEmpresa,
            String codigoDenuncia
    );

    Resource cargarArchivo(String objectKey);
}
