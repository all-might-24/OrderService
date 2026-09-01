package com.ecommerceproject.orderservice.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class JwtTokenService implements  ITokenService{

    private final SecretKey secretKey;

    public JwtTokenService(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public boolean validateToken(String token) {
        try {
            getPayload(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public Claims getPayload(String token) {
        return Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
