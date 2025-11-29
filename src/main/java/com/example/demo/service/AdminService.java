package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final String adminUser;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassHash;
    
    public AdminService(@Value("${app.admin.username}") String user, 
                       @Value("${app.admin.password}") String passHash,
                       PasswordEncoder passwordEncoder) {
        this.adminUser = user; 
        this.adminPassHash = passHash;
        this.passwordEncoder = passwordEncoder;
    }
    
    public boolean authenticate(String u, String p) { 
        return adminUser.equals(u) && passwordEncoder.matches(p, adminPassHash); 
    }
}