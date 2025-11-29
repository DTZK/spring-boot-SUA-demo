package com.example.demo.controller;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;

import com.example.demo.security.JwtUtil;
import com.example.demo.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController{
    private final AdminService adminService;
    private final JwtUtil jwtUtil;

    public AuthController (AdminService a, JwtUtil j){
        adminService = a;
        jwtUtil = j;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest req){
        if (adminService.authenticate(req.getUsername(), req.getPassword())){
            return ResponseEntity.ok(new AuthResponse(jwtUtil.generateToken(req.getUsername())));
        }
        return ResponseEntity.status(401).build();
    }
}
