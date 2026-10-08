package com.tiendaropa.backend.service;

import com.tiendaropa.backend.dto.LoginRequest;
import com.tiendaropa.backend.dto.LoginResponse;
import com.tiendaropa.backend.model.Administrador;
import com.tiendaropa.backend.model.enums.TipoUsuario;
import com.tiendaropa.backend.repository.AdministradorRepository;
import com.tiendaropa.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * No hay endpoint público de registro para administradores a propósito:
 * la primera cuenta se crea directo en la base de datos (o por un script
 * de datos semilla) para no exponer "crear tienda" abiertamente en el piloto.
 */
@Service
@RequiredArgsConstructor
public class AdministradorAuthService {

    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        Administrador admin = administradorRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }

        String token = jwtService.generarToken(admin.getId(), TipoUsuario.ADMINISTRADOR, admin.getTienda().getId());
        return new LoginResponse(token, TipoUsuario.ADMINISTRADOR);
    }
}
