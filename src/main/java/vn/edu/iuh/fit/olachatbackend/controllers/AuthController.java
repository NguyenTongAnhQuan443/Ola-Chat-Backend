package vn.edu.iuh.fit.olachatbackend.controllers;

import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MessageResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.AuthenticationRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.IntrospectRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.LogoutRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.RefreshRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.AuthenticationResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.IntrospectResponse;
import vn.edu.iuh.fit.olachatbackend.services.AuthenticationService;

import java.text.ParseException;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    MessageResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return MessageResponse.<AuthenticationResponse>builder()
                .message("Đăng nhập thành công")
                .data(result)
                .build();
    }

    @PostMapping("/login/google")
    public MessageResponse<AuthenticationResponse> googleLogin(@RequestBody Map<String, String> request) {
        String idToken = request.get("idToken");
        AuthenticationResponse response = authenticationService.loginWithGoogle(idToken);
        return MessageResponse.<AuthenticationResponse>builder()
                .message("Đăng nhập thành công")
                .data(response)
                .build();
    }

    @PostMapping("/login/facebook")
    public MessageResponse<AuthenticationResponse> facebookLogin(@RequestBody Map<String, String> request) {
        String accessToken = request.get("accessToken");
        AuthenticationResponse response = authenticationService.loginWithFacebook(accessToken);
        return MessageResponse.<AuthenticationResponse>builder()
                .message("Đăng nhập thành công")
                .data(response)
                .build();
    }

    @PostMapping("/introspect")
    MessageResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return MessageResponse.<IntrospectResponse>builder()
                .message("Kiểm tra token thành công")
                .data(result)
                .build();
    }

    @PostMapping("/logout")
    MessageResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return MessageResponse.<Void>builder()
                .message("Đăng xuất thành công")
                .data(null)
                .build();
    }

    @PostMapping("/refresh")
    ResponseEntity<MessageResponse<AuthenticationResponse>> refresh(@RequestBody @Valid RefreshRequest request) throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ResponseEntity.ok(MessageResponse.<AuthenticationResponse>builder()
                .message("Làm mới token thành công")
                .data(result)
                .build());
    }


}
