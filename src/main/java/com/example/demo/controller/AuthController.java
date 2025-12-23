package com.example.demo.controller;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.AdminService;
import com.example.demo.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    private final AdminService adminService;
    private final JwtUtil jwtUtil;
    private final RateLimiterService rateLimiterService;
    
    public AuthController(AdminService adminService, JwtUtil jwtUtil, RateLimiterService rateLimiterService) {
        this.adminService = adminService;
        this.jwtUtil = jwtUtil;
        this.rateLimiterService = rateLimiterService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest req, HttpServletRequest request) {
        String ip = getClientIP(request);
        String identifier = ip; // Use IP as rate limit identifier
        
        // Authenticate
        if (adminService.authenticate(req.getUsername(), req.getPassword())) {
            // Success - reset rate limit attempts
            rateLimiterService.resetAttempts(identifier);
            logger.info("Successful login: username={}, ip={}", req.getUsername(), ip);
            
            // Generate both access and refresh tokens
            String accessToken = jwtUtil.generateAccessToken(req.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(req.getUsername());
            
            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
        }
        
        // Failed - record attempt
        rateLimiterService.recordAttempt(identifier);
        int remaining = rateLimiterService.getRemainingAttempts(identifier);
        
        logger.warn("Failed login attempt: username={}, ip={}, remaining={}", req.getUsername(), ip, remaining);
        
        // Build error response
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Invalid credentials");
        
        if (remaining > 0) {
            error.put("attemptsRemaining", remaining);
        } else {
            error.put("message", "Account temporarily locked due to too many failed attempts");
        }
        
        return ResponseEntity.status(401).body(error);
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest req, HttpServletRequest request) {
        String ip = getClientIP(request);
        String refreshToken = req.getRefreshToken();
        
        // Validate it's a refresh token
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            logger.warn("Invalid token type for refresh: ip={}", ip);
            return ResponseEntity.status(401).body(Map.of("error", "Invalid refresh token"));
        }
        
        // Validate and extract username
        String username = jwtUtil.validateAndGetUsername(refreshToken);
        
        if (username != null) {
            // Generate new access token
            String newAccessToken = jwtUtil.generateAccessToken(username);
            logger.info("Token refreshed: username={}, ip={}", username, ip);
            
            // Return new access token with the same refresh token
            return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken));
        }
        
        logger.warn("Failed token refresh: ip={}", ip);
        return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired refresh token"));
    }
    
    private String getClientIP(HttpServletRequest request) {
        // Get real IP address (handles proxy/load balancer)
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        // If multiple IPs in X-Forwarded-For, get the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}