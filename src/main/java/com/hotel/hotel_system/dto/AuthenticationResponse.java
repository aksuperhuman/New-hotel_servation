package com.hotel.hotel_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {

    private String token;
    private Long userId;
    private String name;
    private String email;
    private String role;

    public AuthenticationResponse(String token) {
        this.token = token;
    }
}
