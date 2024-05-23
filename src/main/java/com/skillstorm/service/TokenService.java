package com.skillstorm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    
    private JwtDecoder jwtDecoder;

    public Jwt decodeJwt(String token) {
        return jwtDecoder.decode(token);
    }
}
