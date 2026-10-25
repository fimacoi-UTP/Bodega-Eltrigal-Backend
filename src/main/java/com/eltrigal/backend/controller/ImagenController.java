package com.eltrigal.backend.controller;

import com.eltrigal.backend.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador REST para la subida de imágenes (productos, avatares, etc.).
 * Expone endpoints bajo /api/imagenes.
 */
@RestController
@RequestMapping("/api/imagenes")
@Slf4j
public class ImagenController {

    @Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    /**
     * Sube un archivo de imagen al servidor y devuelve su URL estática.
     * POST /api/imagenes -> 201 Created con { "url": "/uploads/<nombreGenerado>" }
     *
     * @param archivo Archivo multipart tipo imagen
     * @return 201 Created con mapa {"url": ...} o 400 Bad Request
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> subirImagen(@RequestParam("archivo") MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BadRequestException("El archivo es obligatorio y no puede estar vacío");
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new BadRequestException("El archivo debe ser una imagen válida (image/*)");
        }

        String originalFilename = archivo.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        } else if (contentType.contains("/")) {
            String subType = contentType.substring(contentType.indexOf("/") + 1).toLowerCase();
            extension = "." + ("jpeg".equals(subType) ? "jpg" : subType);
        } else {
            extension = ".jpg";
        }

        String nombreGenerado = UUID.randomUUID().toString() + extension;

        try {
            Path uploadPath = Paths.get(uploadsDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path destino = uploadPath.resolve(nombreGenerado);
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            String url = "/uploads/" + nombreGenerado;
            log.info("Imagen almacenada exitosamente: {}", url);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("url", url));
        } catch (IOException e) {
            log.error("Error al guardar la imagen en disco: {}", e.getMessage(), e);
            throw new RuntimeException("Error interno al almacenar la imagen en el servidor", e);
        }
    }

}
