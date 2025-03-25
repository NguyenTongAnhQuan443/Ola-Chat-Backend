package vn.edu.iuh.fit.olachatbackend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String TOKEN_KEY_PREFIX = "invalidated_token:";

    public void saveInvalidatedToken(String tokenId, String token) {
        redisTemplate.opsForValue().set(TOKEN_KEY_PREFIX + tokenId, token);
    }

    public boolean isTokenInvalidated(String tokenId) {
        return redisTemplate.hasKey(TOKEN_KEY_PREFIX + tokenId);
    }
}
