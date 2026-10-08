package com.tiendaropa.backend.security;

import com.tiendaropa.backend.model.enums.TipoUsuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Emite y valida los JWT tanto para administradores como para compradores.
 * El claim "tipo" (ADMINISTRADOR / COMPRADOR) es lo que separa los dos
 * roles a la hora de autorizar una petición — ver JwtAuthFilter.
 */
@Component
public class JwtService {

    private final SecretKey key;
    private final long expiracionMinutos;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiracion-minutos}") long expiracionMinutos) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracionMinutos = expiracionMinutos;
    }

    public String generarToken(Long id, TipoUsuario tipo, Long tiendaId) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expiracionMinutos * 60_000);

        return Jwts.builder()
                .subject(String.valueOf(id))
                .claims(Map.of(
                        "tipo", tipo.name(),
                        "tiendaId", tiendaId == null ? -1 : tiendaId
                ))
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(key)
                .compact();
    }

    public Claims parsearClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
