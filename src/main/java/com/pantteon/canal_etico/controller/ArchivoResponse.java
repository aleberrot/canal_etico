package com.pantteon.canal_etico.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.*;
import java.nio.charset.StandardCharsets;

final class ArchivoResponse {
    private ArchivoResponse() {}

    static ResponseEntity<Resource> descargar(Resource file) {
        String filename = file.getFilename() == null ? "documento" : file.getFilename();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(filename, StandardCharsets.UTF_8).build().toString())
                .body(file);
    }
}
