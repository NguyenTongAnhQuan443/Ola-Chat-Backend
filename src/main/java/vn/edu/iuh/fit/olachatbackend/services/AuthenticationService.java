package vn.edu.iuh.fit.olachatbackend.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.AuthenticationRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.IntrospectRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.LogoutRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.RefreshRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.AuthenticationResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.IntrospectResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.InvalidatedToken;
import vn.edu.iuh.fit.olachatbackend.enums.AuthProvider;
import vn.edu.iuh.fit.olachatbackend.entities.User;
import vn.edu.iuh.fit.olachatbackend.enums.Role;
import vn.edu.iuh.fit.olachatbackend.enums.UserStatus;
import vn.edu.iuh.fit.olachatbackend.exceptions.ConflicException;
import vn.edu.iuh.fit.olachatbackend.exceptions.InternalServerErrorException;
import vn.edu.iuh.fit.olachatbackend.exceptions.UnauthorizedException;
import vn.edu.iuh.fit.olachatbackend.repositories.UserRepository;


import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Slf4j
public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${google.googleClientId}")
    private String googleClientId;

    @Value("${google.androidClientId}")
    private String androidClientId;

    @Value("${facebook.client-id}")
    private String facebookClientId;

    @Value("${facebook.client-secret}")
    private String facebookClientSecret;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        log.info("Token: " + token);

        try {
            verifyToken(token, false);

        } catch (UnauthorizedException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .userId(isValid ? SignedJWT.parse(token).getJWTClaimsSet().getSubject() : null)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Sai tên đăng nhập hoặc mật khẩu"));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new UnauthorizedException("Sai tên đăng nhập hoặc mật khẩu");

        var token = generateToken(user);

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);

            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

//            invalidatedTokenRepository.save(invalidatedToken);
            redisService.saveInvalidatedToken(jit, request.getToken());
        } catch (UnauthorizedException exception) {

            log.info("Token already expired");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedJWT = verifyToken(request.getToken(), true);

        var jit = signedJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

//        invalidatedTokenRepository.save(invalidatedToken);

        if (redisService.isTokenInvalidated(jit)) {
            throw new UnauthorizedException("Token đã bị vô hiệu hóa");
        }

        var username = signedJWT.getJWTClaimsSet().getSubject();

        var user =
                userRepository.findByUsername(username).orElseThrow(() -> new UnauthorizedException("Sai tên đăng nhập hoặc mật khẩu"));

        var token = generateToken(user);

        return AuthenticationResponse.builder().token(token).authenticated(true).build();
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("zycute")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        Objects.requireNonNull(token, "Token không được null");
        Objects.requireNonNull(SIGNER_KEY, "SIGNER_KEY không được null");

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        if (!signedJWT.verify(verifier)) {
            throw new UnauthorizedException("Token không hợp lệ");
        }

        Date expiryTime;
        var claims = signedJWT.getJWTClaimsSet();

        if (isRefresh) {
            Date issueTime = claims.getIssueTime();
            if (issueTime == null) {
                throw new UnauthorizedException("Thiếu issueTime trong refresh token");
            }
            expiryTime = new Date(issueTime.toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli());
        } else {
            expiryTime = claims.getExpirationTime();
        }

        if (expiryTime == null || expiryTime.before(new Date())) {
            throw new UnauthorizedException("Token đã hết hạn");
        }

        if (redisService.isTokenInvalidated(claims.getJWTID())) {
            throw new UnauthorizedException("Token đã bị vô hiệu hóa");
        }

        return signedJWT;
    }


    private String buildScope(User user) {
        if (user.getRole() == null) {
            return "";
        }

        return "ROLE_" + user.getRole().name();
    }

    public AuthenticationResponse loginWithGoogle(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Arrays.asList(googleClientId, androidClientId))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);
            if (googleIdToken == null) {
                throw new UnauthorizedException("Invalid ID token");
            }

            // Lấy thông tin người dùng từ payload
            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            // Kiểm tra hoặc tạo người dùng
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> createGoogleUser(email, name, picture));

            if (user.getAuthProvider() != AuthProvider.GOOGLE) {
                throw new ConflicException("Email already exists with different provider");
            }

            return new AuthenticationResponse(generateToken(user), true);
        } catch (Exception e) {
            throw new InternalServerErrorException("Error verifying token: " + e.getMessage());
        }
    }

    public AuthenticationResponse loginWithFacebook(String accessToken) {
        try {
            String url = "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=" + accessToken;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new UnauthorizedException("Token không hợp lệ");
            }

            // Lấy dữ liệu từ Facebook API
            Map<String, Object> data = response.getBody();
            String facebookId = (String) data.get("id");
            String name = (String) data.get("name");
            String email = (String) data.get("email");
            String picture = (String) ((Map<String, Object>) ((Map<String, Object>) data.get("picture")).get("data")).get("url");

            // Kiểm tra hoặc tạo người dùng mới
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> createFacebookUser(email, name, picture, facebookId));

            return new AuthenticationResponse(generateToken(user), true);
        } catch (Exception e) {
            throw new UnauthorizedException("Lỗi xác thực Facebook");
        }
    }

    private User createGoogleUser(String email, String name, String picture) {
        User user = User.builder()
                .email(email)
                .username(email) // Dùng email làm username
                .displayName(name)
                .avatar(picture)
                .authProvider(AuthProvider.GOOGLE)
                .status(UserStatus.ACTIVE)
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
        return userRepository.save(user);
    }

    private User createFacebookUser(String email, String name, String picture, String facebookId) {
        User user = User.builder()
                .email(email)
                .username(email)
                .displayName(name)
                .avatar(picture)
                .authProvider(AuthProvider.FACEBOOK)
                .status(UserStatus.ACTIVE)
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
        return userRepository.save(user);
    }
}
