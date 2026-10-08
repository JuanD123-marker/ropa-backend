package com.tiendaropa.backend.controller;

import com.tiendaropa.backend.dto.LoginRequest;
import com.tiendaropa.backend.dto.LoginResponse;
import com.tiendaropa.backend.dto.RegistroCompradorRequest;
import com.tiendaropa.backend.service.CompradorAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** El registro es opcional: comprar como invitado no pasa por aquí. */
@RestController
@RequestMapping("/api/comprador/auth")
@RequiredArgsConstructor
public class CompradorAuthController {

    private final CompradorAuthService compradorAuthService;

    @PostMapping("/registro")
    public LoginResponse registrar(@Valid @RequestBody RegistroCompradorRequest request) {
        return compradorAuthService.registrar(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return compradorAuthService.login(request);
    }
}
