package com.tiendaropa.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

/**
 * Orquesta la subida de una foto de prenda: la procesa (quitar fondo o no,
 * según elija el admin) y sube el resultado a Supabase Storage.
 */
@Service
@RequiredArgsConstructor
public class FotoService {

    private final FotoProcesamientoService procesamiento;
    private final SupabaseStorageService storage;

    public String procesarYSubir(Long tiendaId, MultipartFile foto, boolean quitarFondo) {
        if (foto == null || foto.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta la foto");
        }

        try {
            byte[] original = foto.getBytes();
            byte[] resultado = quitarFondo
                    ? procesamiento.procesarConFondoLimpio(original)
                    : procesamiento.procesarSinQuitarFondo(original);

            return storage.subir(resultado, "jpg", tiendaId);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer la foto enviada", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "El procesamiento de la foto fue interrumpido", e);
        }
    }
}