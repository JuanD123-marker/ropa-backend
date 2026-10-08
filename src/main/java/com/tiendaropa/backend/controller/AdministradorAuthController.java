package com.tiendaropa.backend.controller;

import com.tiendaropa.backend.dto.LoginRequest;
import com.tiendaropa.backend.dto.LoginResponse;
import com.tiendaropa.backend.service.AdministradorAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdministradorAuthController {

    private final AdministradorAuthService administradorAuthService;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return administradorAuthService.login(request);
    }
}
