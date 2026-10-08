package com.tiendaropa.backend.service;

import com.tiendaropa.backend.dto.LoginRequest;
import com.tiendaropa.backend.dto.LoginResponse;
import com.tiendaropa.backend.dto.RegistroCompradorRequest;
import com.tiendaropa.backend.model.Comprador;
import com.tiendaropa.backend.model.enums.TipoUsuario;
import com.tiendaropa.backend.repository.CompradorRepository;
import com.tiendaropa.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Registrarse es opcional para comprar; este servicio solo aplica a quien decide crear cuenta. */
@Service
@RequiredArgsConstructor
public class CompradorAuthService {

    private final CompradorRepository compradorRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse registrar(RegistroCompradorRequest request) {
        if (compradorRepository.existsByCorreo(request.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo");
        }

        Comprador comprador = Comprador.builder()
                .nombre(request.getNombre())
                .correo(request.getCorreo())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();
        comprador = compradorRepository.save(comprador);

        String token = jwtService.generarToken(comprador.getId(), TipoUsuario.COMPRADOR, null);
        return new LoginResponse(token, TipoUsuario.COMPRADOR);
    }

    public LoginResponse login(LoginRequest request) {
        Comprador comprador = compradorRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), comprador.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }

        String token = jwtService.generarToken(comprador.getId(), TipoUsuario.COMPRADOR, null);
        return new LoginResponse(token, TipoUsuario.COMPRADOR);
    }
}
