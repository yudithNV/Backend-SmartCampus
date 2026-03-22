package com.example.smartcampus.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    @Value("${supabase.storage.bucket:smartcampus-files}")
    private String bucketName;

    private final HttpClient httpClient = HttpClient.newBuilder().build();

    public String uploadFile(MultipartFile file, String folder) throws IOException, InterruptedException {
        String ext       = getExtension(file.getOriginalFilename());
        String path      = folder + "/" + UUID.randomUUID() + "." + ext;
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + path;
        String ct        = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(uploadUrl))
            .header("Authorization", buildAuthHeader())
            .header("Content-Type", ct)
            .header("x-upsert", "true")
            .PUT(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
            .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new RuntimeException("Supabase Storage error " + res.statusCode() + ": " + res.body());
        }

        return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + path;
    }

    public void deleteFile(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) return;
        try {
            String prefix = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/";
            if (!publicUrl.startsWith(prefix)) return;
            String filePath  = publicUrl.substring(prefix.length());
            String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filePath;
            HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(deleteUrl))
                .header("Authorization", buildAuthHeader())
                .DELETE()
                .build();
            httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            System.err.println("Warning: no se pudo eliminar de Supabase: " + e.getMessage());
        }
    }

    /**
     * Construye el header Authorization según el formato de la key:
     * - Keys nuevas de Supabase (sb_secret_...) usan: "Bearer sb_secret_..."
     * - Keys legacy JWT (eyJ...) usan: "Bearer eyJ..."
     * Ambos formatos usan Bearer — Supabase los acepta igual.
     */
    private String buildAuthHeader() {
        return "Bearer " + serviceRoleKey.trim();
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}