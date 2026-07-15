package com.hotel.hotel_system.service;

import com.hotel.hotel_system.dto.AuthenticationResponse;
import com.hotel.hotel_system.dto.LoginRequest;
import com.hotel.hotel_system.dto.RegisterRequest;
import com.hotel.hotel_system.model.Role;
import com.hotel.hotel_system.model.User;
import com.hotel.hotel_system.repository.UserRepository;
import com.hotel.hotel_system.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.valueOf(request.getRole().toUpperCase()));

        User saved = userRepository.save(user);

        String token = jwtService.generateToken(saved.getEmail());

        return new AuthenticationResponse(token, saved.getId(), saved.getName(), saved.getEmail(), saved.getRole().name());
    }

    public AuthenticationResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + request.getEmail()));

        String token = jwtService.generateToken(request.getEmail());

        return new AuthenticationResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}