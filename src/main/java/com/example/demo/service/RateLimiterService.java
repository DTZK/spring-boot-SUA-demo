package com.example.demo.service;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

import org.hibernate.Remove;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RateLimiterService {
    // initialise variables
    private static final Logger logger = LoggerFactory.getLogger(RateLimiterService.class);
    private RedisTemplate<String, String> redisTemplate;
    @Value("${rate.limit.max-attempts:5}") 
    private int maxAttempts;
    @Value("${rate.limit.window-minutes:10}")
    private int windowMinutes;
    @Value("${rate.limit.lockout-minutes:15}")
    private int lockoutMinutes;

    public RateLimiterService(RedisTemplate<String, String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    /**
     * Check if the identifier is allowed to make a request
     */
    public boolean isAllowed(String identifier){
        String lockoutKey = getLockoutKey(identifier);
        String attemptsKey = getAttemptsKey(identifier);

        //Check if locked out
        Boolean keyExists = redisTemplate.hasKey(lockoutKey);
        if (Boolean.TRUE.equals(keyExists)){
            logger.debug("Identifier {} is locked out", identifier);
            return false;
        }
        //count current attempts left in current window
        Long attempts = redisTemplate.opsForList().size(attemptsKey);
        if (attempts == null) {
            attempts = 0L;
        }
        // Clean old attempts (outside the window)
        cleanOldAttempts(identifier);
        
        // Recount after cleanup
        attempts = redisTemplate.opsForList().size(attemptsKey);
        if (attempts == null) {
            attempts = 0L;
        }
        
        return attempts < maxAttempts;
    }

    /**
     * Record a failed attempt
     */
    public void recordAttempt(String identifier){
        String lockoutKey = getLockoutKey(identifier);
        String attemptsKey = getAttemptsKey(identifier);

        long now = System.currentTimeMillis();
        redisTemplate.opsForList().rightPush(attemptsKey, String.valueOf(now));

        redisTemplate.expire(attemptsKey, windowMinutes, TimeUnit.MINUTES);
        // Check if should lock out
        Long attempts = redisTemplate.opsForList().size(attemptsKey);
        if (attempts != null && attempts >= maxAttempts){
            // Lock out the identifier
            redisTemplate.opsForValue().set(lockoutKey, String.valueOf(now), lockoutMinutes, TimeUnit.MINUTES);
            logger.warn("Identifier {} locked out after {} attempts", identifier, attempts);
        }
        
    }

    public void resetAttempts(String identifier){
        String lockoutKey = getLockoutKey(identifier);
        String attemptsKey = getAttemptsKey(identifier);
        redisTemplate.delete(lockoutKey);
        redisTemplate.delete(attemptsKey);
        logger.debug("Reset attempts for identifier {}", identifier);
    }

    public int getRemainingAttempts(String identifier){
        String attemptsKey = getAttemptsKey(identifier);
        cleanOldAttempts(identifier);

        Long attempts = redisTemplate.opsForList().size(attemptsKey);
        if (attempts == null){
            attempts = 0L;
        }
        return Math.max(0, maxAttempts - attempts.intValue());
    }

    // get lockout expiry time
    public LocalDateTime getLockoutExpiry(String identifier){
        String lockoutKey = getLockoutKey(identifier);
        String lockoutTime = redisTemplate.opsForValue().get(lockoutKey);
        if (lockoutTime != null){
            long lockoutTimestamp = Long.parseLong(lockoutTime);
            return LocalDateTime.ofEpochSecond(
                lockoutTimestamp / 1000 + (lockoutMinutes * 60),
                0,
                ZoneOffset.UTC
            );
        }

        return null;
    }

    public void cleanOldAttempts(String identifier){
        String attemptsKey = getAttemptsKey(identifier);
        long cutOffTime = System.currentTimeMillis() - (windowMinutes *60 *1000);

        //Get all attempts
        Long size = redisTemplate.opsForList().size(attemptsKey);
        if (size == null || size==0){
            return;
        }

        // Remove old attempts from the left
        for (int i =0; i <size; i++){
            String timestamp = redisTemplate.opsForList().index(attemptsKey, 0);
            if (timestamp !=null && Long.parseLong(timestamp)<cutOffTime){
                redisTemplate.opsForList().leftPop(attemptsKey);
            } else{
                break;
            }
        }
    }

    private String getAttemptsKey(String identifier){
        return "rate_limit:attempts:" + identifier;
    }

    private String getLockoutKey(String identifier){
        return "rate_limit:lockout:" + identifier;
    }
}
