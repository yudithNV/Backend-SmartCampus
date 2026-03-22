package com.example.smartcampus.controller;

import com.example.smartcampus.service.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final SupabaseStorageService storageService;

    private static final long MAX_IMAGE_SIZE = 5L  * 1024 * 1024; // 5 MB
    private static final long MAX_DOC_SIZE   = 10L * 1024 * 1024; // 10 MB

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
        "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final Set<String> ALLOWED_DOC_EXTENSIONS = Set.of(
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"
    );

    @PostMapping("/upload/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty())
                return ResponseEntity.badRequest().body(Map.of("error", "Archivo vacío"));
            if (file.getSize() > MAX_IMAGE_SIZE)
                return ResponseEntity.badRequest().body(Map.of("error", "La imagen no debe superar 5 MB"));
            String ct = file.getContentType();
            if (ct == null || !ALLOWED_IMAGE_TYPES.contains(ct))
                return ResponseEntity.badRequest().body(Map.of("error", "Formato no válido. Solo JPG, PNG, WEBP, GIF"));

            String url = storageService.uploadFile(file, "images");
            return ResponseEntity.ok(Map.of("url", url));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al subir imagen: " + e.getMessage()));
        }
    }

    @PostMapping("/upload/document")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty())
                return ResponseEntity.badRequest().body(Map.of("error", "Archivo vacío"));
            if (file.getSize() > MAX_DOC_SIZE)
                return ResponseEntity.badRequest().body(Map.of("error", "El documento no debe superar 10 MB"));
            String name = file.getOriginalFilename();
            if (name == null || !name.contains("."))
                return ResponseEntity.badRequest().body(Map.of("error", "Nombre de archivo inválido"));
            String ext = name.substring(name.lastIndexOf('.') + 1).toLowerCase();
            if (!ALLOWED_DOC_EXTENSIONS.contains(ext))
                return ResponseEntity.badRequest().body(Map.of("error", "Formato no válido. Solo PDF, DOC, DOCX, XLS, XLSX, PPT, PPTX, TXT"));

            String url = storageService.uploadFile(file, "documents");
            return ResponseEntity.ok(Map.of("url", url));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error al subir documento: " + e.getMessage()));
        }
    }
}