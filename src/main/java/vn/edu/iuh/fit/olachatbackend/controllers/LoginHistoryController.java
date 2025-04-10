/*
 * @ (#) LoginHistoryController.java       1.0     30/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.controllers;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 30/03/2025
 * @version:    1.0
 */

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.olachatbackend.dtos.LoginHistoryDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MessageResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UserStatusResponse;
import vn.edu.iuh.fit.olachatbackend.services.LoginHistoryService;

import java.util.List;

@RestController
@RequestMapping("/api/login-history")
public class LoginHistoryController {

    private final LoginHistoryService loginHistoryService;

    public LoginHistoryController(LoginHistoryService loginHistoryService) {
        this.loginHistoryService = loginHistoryService;
    }

    @GetMapping("/{userId}")
    public MessageResponse<List<LoginHistoryDTO>> getLoginHistory(@PathVariable String userId) {
        List<LoginHistoryDTO> historyList = loginHistoryService.getLoginHistory(userId);
        return MessageResponse.<List<LoginHistoryDTO>>builder()
                .message("Lịch sử đăng nhập")
                .data(historyList)
                .success(true)
                .statusCode(200)
                .build();
    }


    @GetMapping("/recent/{userId}")
    public ResponseEntity<LoginHistoryDTO> getRecentLogin(@PathVariable String userId) {
        LoginHistoryDTO loginHistoryDTO = loginHistoryService.getRecentLogin(userId);
        return ResponseEntity.ok(loginHistoryDTO);
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<UserStatusResponse> isUserOnline(@PathVariable String userId) {
        boolean isOnline = loginHistoryService.isUserOnline(userId);
        return ResponseEntity.ok(new UserStatusResponse(userId, isOnline));
    }
}
