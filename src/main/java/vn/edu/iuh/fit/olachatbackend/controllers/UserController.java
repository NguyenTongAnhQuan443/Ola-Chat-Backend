/*
 * @ (#) UserController.java       1.0     14/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.controllers;

import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.UserRegisterRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MessageResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UserResponse;
import vn.edu.iuh.fit.olachatbackend.services.UserService;

import java.util.List;

/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 14/02/2025
 * @version:    1.0
 */

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

//    @PostMapping
//    public User createUser(@RequestBody User user) {
//        return userService.saveUser(user);
//    }

    @GetMapping
    public MessageResponse<List<UserResponse>> getAllUsers() {
        return MessageResponse.<List<UserResponse>>builder()
                .message("Lấy danh sách người dùng thành công")
                .data(userService.getUsers())
                .build();
    }

    @PostMapping
    public MessageResponse<UserResponse> registerUser(@RequestBody @Valid UserRegisterRequest request) {
        return MessageResponse.<UserResponse>builder()
                .message("Đăng ký người dùng thành công")
                .data(userService.registerUser(request))
                .build();
    }

    @GetMapping("/myInfo")
    public MessageResponse<UserResponse> getMyInfo() {
        return MessageResponse.<UserResponse>builder()
                .message("Lấy thông tin cá nhân thành công")
                .data(userService.getMyInfo())
                .build();
    }

    @GetMapping("/me")
    public MessageResponse<UserResponse> getMyInfo(@RequestHeader("Authorization") String token) {
        return MessageResponse.<UserResponse>builder()
                .message("Lấy thông tin cá nhân thành công")
                .data(userService.getMyInfo(token))
                .build();
    }

}
