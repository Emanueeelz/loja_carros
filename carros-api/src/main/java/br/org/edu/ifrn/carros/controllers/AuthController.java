package br.org.edu.ifrn.carros.controllers;

import br.org.edu.ifrn.carros.dto.LoginRequest;
import br.org.edu.ifrn.carros.dto.LoginResponse;
import br.org.edu.ifrn.carros.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }
}
