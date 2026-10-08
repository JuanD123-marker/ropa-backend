package com.tiendaropa.backend.security;

import com.tiendaropa.backend.model.enums.TipoUsuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee el header Authorization, valida el JWT y arma la autenticación con
 * ROLE_ADMINISTRADOR o ROLE_COMPRADOR según el claim "tipo". No consulta la
 * base de datos en cada petición (suficiente para el piloto); si el token
 * falta o es inválido, simplemente no autentica y deja que SecurityConfig
 * decida si esa ruta requiere login.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

   @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        System.out.println("[DEBUG] Authorization header recibido: [" + header + "]");

    if (header != null && header.startsWith("Bearer ")) {
        String token = header.substring(7);
        try {
            Claims claims = jwtService.parsearClaims(token);
            Long id = Long.valueOf(claims.getSubject());
            TipoUsuario tipo = TipoUsuario.valueOf(claims.get("tipo", String.class));
            Long tiendaId = claims.get("tiendaId", Number.class) != null
                    ? claims.get("tiendaId", Number.class).longValue()
                    : null;

            UsuarioAutenticado usuario = new UsuarioAutenticado(id, tipo, tiendaId);

            var authoridad = new SimpleGrantedAuthority("ROLE_" + tipo.name());
            var auth = new UsernamePasswordAuthenticationToken(usuario, null, List.of(authoridad));
            SecurityContextHolder.getContext().setAuthentication(auth);

            System.out.println("[DEBUG] Autenticado OK. Authorities=" + auth.getAuthorities()
                    + " isAuthenticated=" + auth.isAuthenticated());
        } catch (JwtException | IllegalArgumentException ex) {
            System.out.println("[DEBUG] Error al parsear JWT: " + ex);
            SecurityContextHolder.clearContext();
        }
    } else {
        System.out.println("[DEBUG] Header nulo o no empieza con 'Bearer '");
    }

    filterChain.doFilter(request, response);

}

}
