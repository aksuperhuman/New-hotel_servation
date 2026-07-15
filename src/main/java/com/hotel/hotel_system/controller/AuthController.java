package com.hotel.hotel_system.controller;

import com.hotel.hotel_system.dto.AuthenticationResponse;
import com.hotel.hotel_system.dto.LoginRequest;
import com.hotel.hotel_system.dto.RegisterRequest;
import com.hotel.hotel_system.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @PostMapping("/register")
    public AuthenticationResponse register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration requested email={}", request.getEmail());
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthenticationResponse login(@Valid @RequestBody LoginRequest request) {
        log.info("Login requested email={}", request.getEmail());
        return authService.login(request);
    }
}
