package com.example.demo.controller;

import com.example.demo.service.RateLimiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class RateLimiterTestController {
    
    @Autowired
    private RateLimiterService rateLimiterService;
    
    @GetMapping("/check/{identifier}")
    public ResponseEntity<Map<String, Object>> checkRateLimit(@PathVariable String identifier) {
        Map<String, Object> response = new HashMap<>();
        
        boolean allowed = rateLimiterService.isAllowed(identifier);
        int remaining = rateLimiterService.getRemainingAttempts(identifier);
        LocalDateTime lockoutExpiry = rateLimiterService.getLockoutExpiry(identifier);
        
        response.put("allowed", allowed);
        response.put("remainingAttempts", remaining);
        response.put("lockoutExpiry", lockoutExpiry);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/attempt/{identifier}")
    public ResponseEntity<Map<String, Object>> recordAttempt(@PathVariable String identifier) {
        if (!rateLimiterService.isAllowed(identifier)) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Rate limit exceeded");
            response.put("lockoutExpiry", rateLimiterService.getLockoutExpiry(identifier));
            return ResponseEntity.status(429).body(response);
        }
        
        rateLimiterService.recordAttempt(identifier);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Attempt recorded");
        response.put("remainingAttempts", rateLimiterService.getRemainingAttempts(identifier));
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/reset/{identifier}")
    public ResponseEntity<String> resetAttempts(@PathVariable String identifier) {
        rateLimiterService.resetAttempts(identifier);
        return ResponseEntity.ok("Attempts reset for " + identifier);
    }
}