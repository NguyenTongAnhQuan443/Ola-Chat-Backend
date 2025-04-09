package vn.edu.iuh.fit.olachatbackend.configs;

import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.edu.iuh.fit.olachatbackend.services.RedisService;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

@Component
public class JwtTokenValidatorFilter extends OncePerRequestFilter {

    @Autowired
    private RedisService redisService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                SignedJWT signedJWT = SignedJWT.parse(token);
                var claims = signedJWT.getJWTClaimsSet();

                String jit = claims.getJWTID();

                // ✅ 1. Check expired
                if (claims.getExpirationTime() != null &&
                        claims.getExpirationTime().before(new Date())) {
                    throw new JwtException("Token đã hết hạn");
                }

                // ✅ 2. Check blacklist (đã logout)
                if (redisService.isTokenBlacklisted(jit)) {
                    throw new JwtException("Token đã bị thu hồi");
                }

                // ✅ 3. Optional: Check whitelist nếu cần (ví dụ refresh token)
                // Nếu muốn phân biệt refresh token qua scope:
                /*
                String scope = (String) claims.getClaim("scope");
                if (scope != null && scope.contains("REFRESH")) {
                    if (!redisService.isTokenWhitelisted(jit)) {
                        throw new JwtException("Refresh token không hợp lệ hoặc đã bị thu hồi");
                    }
                }
                */

            } catch (ParseException | JwtException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setCharacterEncoding("UTF-8");
                response.setContentType("application/json; charset=UTF-8");
                response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
