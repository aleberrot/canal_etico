package com.pantteon.canal_etico.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StorageKeyGenerator {

    public String generar(
            String codigoEmpresa,
            String codigoDenuncia,
            String originalFilename
    ) {
        return "denuncias/%s/%s/%s-%s".formatted(
                sanitizarSegmento(codigoEmpresa),
                sanitizarSegmento(codigoDenuncia),
                UUID.randomUUID(),
                sanitizarNombre(originalFilename)
        );
    }

    public String obtenerNombreDescarga(String objectKey) {
        String nombre = objectKey.replace('\\', '/');
        nombre = nombre.substring(nombre.lastIndexOf('/') + 1);
        if (nombre.length() > 37 && nombre.charAt(36) == '-') {
            return nombre.substring(37);
        }
        return nombre;
    }

    private String sanitizarSegmento(String valor) {
        if (valor == null || valor.isBlank()) {
            return "sin-codigo";
        }
        String sanitizado = valor.replaceAll("[^A-Za-z0-9_-]", "-");
        if (sanitizado.isBlank()) {
            return "sin-codigo";
        }
        return sanitizado.substring(0, Math.min(sanitizado.length(), 40));
    }

    private String sanitizarNombre(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "archivo";
        }
        String nombre = originalFilename.replace('\\', '/');
        nombre = nombre.substring(nombre.lastIndexOf('/') + 1);
        nombre = nombre.replaceAll("[^\\p{L}\\p{N}._-]", "_");
        nombre = nombre.replaceFirst("^\\.+", "");
        if (nombre.isBlank()) {
            return "archivo";
        }
        return limitarNombreConExtension(nombre, 100);
    }

    private String limitarNombreConExtension(String nombre, int maximo) {
        if (nombre.length() <= maximo) {
            return nombre;
        }
        int punto = nombre.lastIndexOf('.');
        String extension = punto > 0 && nombre.length() - punto <= 20
                ? nombre.substring(punto)
                : "";
        return nombre.substring(0, maximo - extension.length()) + extension;
    }
}
