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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

@Component
public class JwtTokenValidatorFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenValidatorFilter.class);
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

                if (claims.getExpirationTime() != null &&
                        claims.getExpirationTime().before(new Date())) {
                    throw new JwtException("Token đã hết hạn");
                }

                log.info("🛡️ Kiểm tra token jti: {}", jit);

                if (redisService.isTokenBlacklisted(jit)) {
                    log.warn("🚫 Token {} đã bị thu hồi – từ chối request", jit);
                    throw new JwtException("Access token đã bị thu hồi");
                }



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
