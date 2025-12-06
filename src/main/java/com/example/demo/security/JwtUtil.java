package com.example.demo.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-ms}") long expirationMs) {

        if (secret.length() < 32) {
        throw new IllegalArgumentException("JWT secret must be at least 32 characters (256 bits)");
    }
    
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String username) {
        Date now = new Date();
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + expirationMs))
            .signWith(key)
            .compact();
    }

    public String validateAndGetUsername(String token) {
        try {
            return Jwts.parserBuilder()
                       .setSigningKey(key)
                       .build()
                       .parseClaimsJws(token)
                       .getBody()
                       .getSubject();
        } catch (JwtException e) {
            return null;
        }
    }
}