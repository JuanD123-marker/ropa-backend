package com.tiendaropa.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

/**
 * Sube las fotos ya procesadas al bucket público de Supabase Storage y
 * devuelve la URL pública permanente. La "secret key" del proyecto nunca se
 * hardcodea: viene de una variable de entorno (igual que el secreto del JWT
 * o la contraseña de la base de datos).
 */
@Service
public class SupabaseStorageService {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final String supabaseUrl;
    private final String bucket;
    private final String secretKey;

    public SupabaseStorageService(
            @Value("${app.supabase.storage.url}") String supabaseUrl,
            @Value("${app.supabase.storage.bucket}") String bucket,
            @Value("${app.supabase.storage.secret-key}") String secretKey) {
        this.supabaseUrl = supabaseUrl;
        this.bucket = bucket;
        this.secretKey = secretKey;
    }

    /** Sube los bytes de la imagen bajo la carpeta de la tienda y devuelve su URL pública. */
    public String subir(byte[] contenido, String extension, Long tiendaId) {
        String nombreArchivo = UUID.randomUUID() + "." + extension;
        String rutaObjeto = "prendas/" + tiendaId + "/" + nombreArchivo;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(supabaseUrl + "/storage/v1/object/" + bucket + "/" + rutaObjeto))
                    .header("Authorization", "Bearer " + secretKey)
                    .header("apikey", secretKey)
                    .header("Content-Type", "image/" + extension)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(contenido))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() / 100 != 2) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Supabase Storage respondió " + response.statusCode() + ": " + response.body());
            }

            return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + rutaObjeto;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo subir la foto a Supabase Storage", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "La subida a Supabase Storage fue interrumpida", e);
        }
    }
}