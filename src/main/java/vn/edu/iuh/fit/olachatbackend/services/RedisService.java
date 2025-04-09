package vn.edu.iuh.fit.olachatbackend.services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String ACCESS_REF_PREFIX = "access_ref:";

    public void saveAccessTokenReference(String userId, String deviceId, String jti, long duration, TimeUnit unit) {
        redisTemplate.opsForValue().set(ACCESS_REF_PREFIX + userId + ":" + deviceId, jti, duration, unit);
    }

    public String getAccessTokenJit(String userId, String deviceId) {
        return redisTemplate.opsForValue().get(ACCESS_REF_PREFIX + userId + ":" + deviceId);
    }


    public void saveWhitelistedToken(String jit, String token, long duration, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + jit, token, duration, timeUnit);
    }

    public boolean isTokenWhitelisted(String jit) {
        return redisTemplate.hasKey(REFRESH_TOKEN_PREFIX + jit);
    }

    public void removeWhitelistedToken(String jit) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + jit);
    }

    public void addBlacklistedToken(String jit, long duration, TimeUnit unit) {
        redisTemplate.opsForValue().set("blacklist:" + jit, "revoked", duration, unit);
    }

    public boolean isTokenBlacklisted(String jit) {
        return redisTemplate.hasKey("blacklist:" + jit);
    }

    public void removeAccessTokenReference(String userId, String deviceId) {
        redisTemplate.delete("access_ref:" + userId + ":" + deviceId);
    }


    //otp email
    private static final long OTP_EXPIRE_SECONDS = 300; // 5 phút

    public void saveOtp(String email, String otpCode) {
        redisTemplate.opsForValue().set("OTP" + email, otpCode, OTP_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    public String getOtp(String email) {
        String key = "OTP" + email;
        System.out.println("🔍 Đang lấy OTP với key: " + key);
        System.out.println(redisTemplate.opsForValue().get("vy"));
        return redisTemplate.opsForValue().get(key);

    }

    public void deleteOtp(String email) {
        redisTemplate.delete("OTP:" + email);
    }


}

