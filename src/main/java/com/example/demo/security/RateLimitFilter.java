package com.example.demo.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.service.RateLimiterService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    
    private final RateLimiterService rateLimiterService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public RateLimitFilter(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Only apply to login endpoint
        if (!request.getRequestURI().equals("/api/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Use IP address as identifier (or username if available)
        String identifier = getClientIdentifier(request);
        
        // Check if allowed
        if (!rateLimiterService.isAllowed(identifier)) {
            sendRateLimitError(response, identifier);
            return;
        }
        
        // Continue with the request
        filterChain.doFilter(request, response);
    }
    
    private String getClientIdentifier(HttpServletRequest request) {
        // Try to get real IP (in case behind proxy)
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    private void sendRateLimitError(HttpServletResponse response, String identifier) throws IOException {
        response.setStatus(429); // Too Many Requests
        response.setContentType("application/json");
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", "Too many login attempts");
        error.put("message", "Please try again later");
        
        LocalDateTime lockoutExpiry = rateLimiterService.getLockoutExpiry(identifier);
        if (lockoutExpiry != null) {
            error.put("retryAfter", lockoutExpiry.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}