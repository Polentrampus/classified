package com.classified.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
    Long userId;
    String username;
    String email;
    String token;

    public AuthResponse(String token, Long id, String username) {
        this.token = token;
        this.userId = id;
        this.username = username;
    }
}
