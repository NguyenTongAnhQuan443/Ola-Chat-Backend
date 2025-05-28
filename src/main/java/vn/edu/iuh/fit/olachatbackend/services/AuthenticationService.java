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
import vn.edu.iuh.fit.olachatbackend.dtos.responses.AuthenticationResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.IntrospectResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UserResponse;
import vn.edu.iuh.fit.olachatbackend.enums.AuthProvider;
import vn.edu.iuh.fit.olachatbackend.entities.User;
import vn.edu.iuh.fit.olachatbackend.exceptions.BadRequestException;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.enums.Role;
import vn.edu.iuh.fit.olachatbackend.enums.UserStatus;
import vn.edu.iuh.fit.olachatbackend.exceptions.ConflicException;
import vn.edu.iuh.fit.olachatbackend.exceptions.UnauthorizedException;
import vn.edu.iuh.fit.olachatbackend.mappers.UserMapper;
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
public class AuthenticationService {
    @Autowired
    private UserRepository userRepository;

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
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();
        boolean isValid = true;
        String userId = null;

        log.info("Token: " + token);

        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String type = signedJWT.getJWTClaimsSet().getStringClaim("type");
            boolean isRefresh = "refresh".equals(type);

            verifyToken(token, isRefresh);

            String jit = signedJWT.getJWTClaimsSet().getJWTID();

            if (isRefresh) {
                if (!redisService.isTokenWhitelisted(jit)) {
                    isValid = false;
                }
            } else {
                if (redisService.isTokenBlacklisted(jit)) {
                    isValid = false;
                }
            }

            userId = signedJWT.getJWTClaimsSet().getSubject();

        } catch (Exception e) {
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



        loginHistoryService.saveLogin(user.getId(), deviceId);

        return AuthenticationResponse
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userMapper.toUserResponse(user))
                .authenticated(true).build();
    }

    public void logout(LogoutRequest request, HttpServletResponse response) throws ParseException, JOSEException {
        String accessToken = request.getAccessToken();
        String refreshToken = request.getRefreshToken();

        try {
            // Parse & verify refresh token
            SignedJWT refreshSignedToken = verifyToken(refreshToken, true);

            // Parse access token thủ công để lấy thông tin (vì có thể hết hạn)
            SignedJWT accessSignedToken = SignedJWT.parse(accessToken);
            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            if (!accessSignedToken.verify(verifier)) {
                throw new BadRequestException("Access token không hợp lệ");
            }

            // Lấy claims
            JWTClaimsSet refreshClaims = refreshSignedToken.getJWTClaimsSet();
            JWTClaimsSet accessClaims = accessSignedToken.getJWTClaimsSet();

            String refreshDeviceId = refreshClaims.getStringClaim("deviceId");
            String accessDeviceId = accessClaims.getStringClaim("deviceId");

            if (!Objects.equals(refreshDeviceId, accessDeviceId)) {
                throw new BadRequestException("Token không cùng thiết bị");
            }

            // Xóa refresh token khỏi whitelist
            String refreshJit = refreshClaims.getJWTID();
            redisService.removeWhitelistedToken(refreshJit);

            // Thêm access token vào blacklist (chỉ khi có hạn sử dụng)
            String accessJit = accessClaims.getJWTID();
            Date expiration = accessClaims.getExpirationTime();
            if (expiration != null && expiration.after(new Date())) {
                long ttl = (expiration.getTime() - System.currentTimeMillis()) / 1000;
                redisService.addBlacklistedToken(accessJit, ttl, TimeUnit.SECONDS);
            }

            // Xóa cookie
            Cookie refreshTokenCookie = new Cookie("refreshToken", null);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(true);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(0);
            response.addCookie(refreshTokenCookie);

            String username = refreshClaims.getSubject();
            Optional<User> user = userRepository.findByUsername(username);

            if (user.isEmpty()) {
                throw new NotFoundException("Không thể tìm thấy user!");
            }
            loginHistoryService.saveLogout(user.map(User::getId).orElse(null));

            // remove device
            notificationService.removeDevice(user.get().getId(), accessDeviceId);

        } catch (UnauthorizedException e) {
            throw new BadRequestException("Token không hợp lệ hoặc đã hết hạn");
        }
    }

    public AuthenticationResponse refreshToken(String refreshToken, HttpServletResponse response) throws ParseException, JOSEException {
        // 1. Verify refreshToken
        var signedJWT = verifyToken(refreshToken, true);

        var username = signedJWT.getJWTClaimsSet().getSubject();
        var deviceId = signedJWT.getJWTClaimsSet().getStringClaim("deviceId");

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Người dùng không tồn tại"));

        // 2. Kiểm tra trong Redis
        String oldJit = signedJWT.getJWTClaimsSet().getJWTID();
        if (!redisService.isTokenWhitelisted(oldJit)) {
            throw new UnauthorizedException("Refresh token không hợp lệ hoặc đã bị thu hồi");
        }

        // 3. Sinh accessToken mới
        var newAccessToken = generateToken(user, deviceId, false);

        // 4. Tuỳ chọn: tạo refreshToken mới (an toàn hơn)
        var newRefreshToken = generateToken(user, deviceId, true);
        String newJit = SignedJWT.parse(newRefreshToken).getJWTClaimsSet().getJWTID();

        // 5. Cập nhật Redis
        redisService.removeWhitelistedToken(oldJit); // xóa cũ
        redisService.saveWhitelistedToken(newJit, newRefreshToken, 7, TimeUnit.DAYS); // lưu mới

        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .authenticated(true)
                .build();
    }

    private String generateToken(User user, String deviceId, boolean isRefreshToken) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        Instant now = Instant.now();
        long duration = isRefreshToken ? REFRESHABLE_DURATION : VALID_DURATION;
        Date expiryDate = Date.from(now.plus(duration, ChronoUnit.SECONDS));

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer("zycute")
                .claim("userId", user.getId())
                .issueTime(Date.from(now))
                .expirationTime(expiryDate)
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .claim("deviceId", deviceId)
                .claim("type", isRefreshToken ? "refresh" : "access")
                .build();

        Payload payload = new Payload(claimsSet.toJSONObject());

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

        SignedJWT signedJWT;
        try {
            signedJWT = SignedJWT.parse(token);
        } catch (ParseException e) {
            throw new UnauthorizedException("Token sai định dạng");
        }

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

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

        String jit = claims.getJWTID();

        if (isRefresh && !redisService.isTokenWhitelisted(jit)) {
            throw new UnauthorizedException("Refresh token không hợp lệ hoặc đã bị thu hồi");
        }

        // ✅ Check chỉ cho access token: không bị đưa vào blacklist
        if (!isRefresh && redisService.isTokenBlacklisted(jit)) {
            throw new UnauthorizedException("Access token đã bị thu hồi");
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

            loginHistoryService.saveLogin(user.getId(), deviceId);

            var accessToken = generateToken(user, deviceId, false);
            var refreshToken = generateToken(user, deviceId, true);
            // Lưu refresh token vào Redis (whitelist)
            String jit = SignedJWT.parse(refreshToken).getJWTClaimsSet().getJWTID();
            redisService.saveWhitelistedToken(jit, refreshToken, 7, TimeUnit.DAYS);

            return  AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .authenticated(true)
                    .user(userMapper.toUserResponse(user))
                    .build();
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

            loginHistoryService.saveLogin(user.getId(), deviceId);


            var accessTokenServerReturn = generateToken(user, deviceId, false);
            var refreshToken = generateToken(user, deviceId, true);
            // Lưu refresh token vào Redis (whitelist)
            String jit = SignedJWT.parse(refreshToken).getJWTClaimsSet().getJWTID();
            redisService.saveWhitelistedToken(jit, refreshToken, 7, TimeUnit.DAYS);

            UserResponse userResponse = userMapper.toUserResponse(user);
            return  AuthenticationResponse.builder()
                    .accessToken(accessTokenServerReturn)
                    .refreshToken(refreshToken)
                    .authenticated(true)
                    .user(userMapper.toUserResponse(user))
                    .build();
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

        String otpRateLimitKey = "OTP_LIMIT:" + email;
        Long lastSentTime = redisService.getLong(otpRateLimitKey);
        long now = System.currentTimeMillis();

//        if (lastSentTime != null && (now - lastSentTime) < 3600_000) { // 1 giờ
//            throw new BadRequestException("Bạn chỉ có thể yêu cầu gửi OTP mỗi 1 giờ. Vui lòng thử lại sau.");
//        }

        String otpCode = OtpUtils.generateOtp();

        redisService.saveOtp(email, otpCode);
        emailService.sendOtpEmail(email, otpCode);

        // Đánh dấu thời điểm gửi OTP, key sẽ hết hạn sau 1 giờ
        redisService.setLong(otpRateLimitKey, now, 1, TimeUnit.HOURS);
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
