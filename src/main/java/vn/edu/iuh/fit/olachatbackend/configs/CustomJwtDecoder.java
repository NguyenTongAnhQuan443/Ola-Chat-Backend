package vn.edu.iuh.fit.olachatbackend.configs;

import com.nimbusds.jose.JOSEException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.IntrospectRequest;
import vn.edu.iuh.fit.olachatbackend.services.AuthenticationService;

import javax.annotation.PostConstruct;
import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.util.Objects;

@Slf4j
@Component
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.signerKey}")
    private String signerKey;

    @Autowired
    private AuthenticationService authenticationService;

    private NimbusJwtDecoder nimbusJwtDecoder;

    @PostConstruct
    public void init() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HmacSHA512");
        nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            if (token == null || token.trim().isEmpty()) {
                log.warn("❌ Token trống hoặc null");
                throw new BadJwtException("Token is missing");
            }

            var response = authenticationService.introspect(
                    IntrospectRequest.builder().token(token).build()
            );

            if (!response.isValid()) {
                log.warn("❌ Token không hợp lệ theo introspect (bị thu hồi hoặc hết hạn)");
                throw new BadJwtException("Token invalid (introspection failed)");
            }

            return nimbusJwtDecoder.decode(token);

        } catch (ParseException | JOSEException e) {
            log.error("❌ Lỗi khi phân tích token: {}", e.getMessage());
            throw new BadJwtException("Token parsing error", e);
        } catch (JwtException e) {
            log.warn("❌ Lỗi JWT: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Lỗi không xác định khi decode token: {}", e.getMessage());
            throw new BadJwtException("Unexpected error during token decoding", e);
        }
    }
}
