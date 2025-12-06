package com.example.demo.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = ""; // Your desired password
        String hashedPassword = encoder.encode(rawPassword);
        System.out.println(hashedPassword);
    }
}