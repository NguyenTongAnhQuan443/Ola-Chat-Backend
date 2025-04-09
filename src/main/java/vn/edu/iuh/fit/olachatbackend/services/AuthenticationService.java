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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.*;
import org.springframework.web.client.RestTemplate;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.AuthenticationRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.IntrospectRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.LogoutRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.RefreshRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.AuthenticationResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.IntrospectResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.InvalidatedToken;
import vn.edu.iuh.fit.olachatbackend.entities.RefreshToken;
import vn.edu.iuh.fit.olachatbackend.enums.AuthProvider;
import vn.edu.iuh.fit.olachatbackend.entities.User;
import vn.edu.iuh.fit.olachatbackend.exceptions.BadRequestException;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.enums.Role;
import vn.edu.iuh.fit.olachatbackend.enums.UserStatus;
import vn.edu.iuh.fit.olachatbackend.exceptions.ConflicException;
import vn.edu.iuh.fit.olachatbackend.exceptions.InternalServerErrorException;
import vn.edu.iuh.fit.olachatbackend.exceptions.UnauthorizedException;
import vn.edu.iuh.fit.olachatbackend.repositories.RefreshTokenRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.UserRepository;
import vn.edu.iuh.fit.olachatbackend.utils.OtpUtils;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private RedisService redisService;
    @Autowired
    private EmailService emailService;

    @Autowired
    private LoginHistoryService loginHistoryService;

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
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;

        log.info("Token: " + token);

        try {
            verifyToken(token, false);

            // Kiểm tra xem token có trong whitelist không
            String jit = SignedJWT.parse(token).getJWTClaimsSet().getJWTID();
            if (!redisService.isTokenWhitelisted(jit)) {
                isValid = false;
            }

        } catch (UnauthorizedException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .userId(isValid ? SignedJWT.parse(token).getJWTClaimsSet().getSubject() : null)
                .build();
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request, HttpServletResponse response) throws ParseException {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        var user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Sai tên đăng nhập hoặc mật khẩu"));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) throw new UnauthorizedException("Sai tên đăng nhập hoặc mật khẩu");

        String deviceId = request.getDeviceId();

        var accessToken = generateToken(user, deviceId, false);
        var refreshToken = generateToken(user, deviceId, true);

        // Lưu refresh token vào Redis (whitelist)
        String jit = SignedJWT.parse(refreshToken).getJWTClaimsSet().getJWTID();
        redisService.saveWhitelistedToken(jit, refreshToken, 7, TimeUnit.DAYS);

        // Thêm refresh token vào HTTP-only cookie
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshTokenCookie);

        loginHistoryService.saveLogin(user.getId());

        return AuthenticationResponse.builder().token(accessToken).authenticated(true).build();
    }

    public void logout(LogoutRequest request, HttpServletResponse response) throws ParseException, JOSEException {
        try {
            var signToken = verifyToken(request.getToken(), true);
            var claims = signToken.getJWTClaimsSet();

            String refreshJti = claims.getJWTID();
            String username = claims.getSubject();
            String deviceId = claims.getStringClaim("deviceId");

            //  1. Xóa refresh token khỏi whitelist
            redisService.removeWhitelistedToken(refreshJti);

            //  2. Tìm access token jti và đưa vào blacklist
            String accessJti = redisService.getAccessTokenJit(username, deviceId);
            if (accessJti != null) {
                redisService.addBlacklistedToken(accessJti, 20, TimeUnit.MINUTES);
            } else {
            }

            // 3. Gỡ cookie refresh token
            Cookie refreshTokenCookie = new Cookie("refreshToken", null);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0);
            response.addCookie(refreshTokenCookie);

            // 4. Ghi log
            Optional<User> user = userRepository.findByUsername(username);
            loginHistoryService.saveLogout(user.map(User::getId).orElse(null));

        } catch (UnauthorizedException exception) {
            log.info("❌ Token đã hết hạn hoặc không hợp lệ");
        }
    }

    private String generateToken(User user, String deviceId, boolean isRefreshToken) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        Instant now = Instant.now();
        long duration = isRefreshToken ? REFRESHABLE_DURATION : VALID_DURATION;
        Date expiryDate = Date.from(now.plus(duration, ChronoUnit.SECONDS));

        String jti = UUID.randomUUID().toString();

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("zycute")
                .issueTime(Date.from(now))
                .expirationTime(expiryDate)
                .jwtID(jti)
                .claim("scope", buildScope(user))
                .claim("deviceId", deviceId)
                .build();

        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            String token = jwsObject.serialize();

            // ✅ Nếu là access token → lưu jti vào Redis
            if (!isRefreshToken) {
                redisService.saveAccessTokenReference(user.getUsername(), deviceId, jti, 20, TimeUnit.MINUTES);
            }

            return token;
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

        if (!redisService.isTokenWhitelisted(claims.getJWTID())) {
            throw new UnauthorizedException("Token không hợp lệ hoặc đã bị thu hồi");
        }

        if (!isRefresh && redisService.isTokenBlacklisted(claims.getJWTID())) {
            throw new UnauthorizedException("Access Token đã bị thu hồi");
        }


        return signedJWT;
    }



    private String buildScope(User user) {
        if (user.getRole() == null) {
            return "";
        }

        return "ROLE_" + user.getRole().name();
    }

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    public AuthenticationResponse loginWithGoogle(String idToken, String deviceId) {
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

            loginHistoryService.saveLogin(user.getId());

            return new AuthenticationResponse(generateToken(user, deviceId, false), true);
        } catch (Exception e) {
            throw new BadRequestException("Lỗi xác thực token: " + e.getMessage());
        }
    }

    public AuthenticationResponse loginWithFacebook(String accessToken, String deviceId) {
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

            loginHistoryService.saveLogin(user.getId());

            return new AuthenticationResponse(generateToken(user, deviceId, false), true);
        } catch (Exception e) {
            throw new BadRequestException("Lỗi xác thực Facebook");
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

    public void processForgotPassword(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new NotFoundException("User không tồn tại");
        }

        String otpCode = OtpUtils.generateOtp();

        redisService.saveOtp(email, otpCode);

        emailService.sendOtpEmail(email, otpCode);
    }

    public void resetPassword(ResetPasswordRequest otpRequest) {
        String otp = otpRequest.getOtp();
        String email = otpRequest.getEmail();
        String newPassword = otpRequest.getNewPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Người dùng không tồn tại."));

        String storedOtp = redisService.getOtp(email);
        if (storedOtp == null) {
            throw new NotFoundException("OTP đã hết hạn hoặc không tồn tại.");
        }

        if (!storedOtp.equals(otp)) {
            throw new BadRequestException("OTP không hợp lệ. Vui lòng thử lại.");
        }




        // Cập nhật mật khẩu mới
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redisService.deleteOtp(email);

    }

}
