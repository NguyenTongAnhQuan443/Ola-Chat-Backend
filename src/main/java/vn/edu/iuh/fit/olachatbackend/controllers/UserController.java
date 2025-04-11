/*
 * @ (#) UserController.java       1.0     14/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.controllers;

import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.ChangePasswordRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.UserUpdateInfoRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.FriendResponse;
import jakarta.validation.Valid;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.UserRegisterRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MessageResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UserResponse;
import vn.edu.iuh.fit.olachatbackend.services.FriendService;
import vn.edu.iuh.fit.olachatbackend.services.UserService;

import java.io.IOException;
import java.util.List;

/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 14/02/2025
 * @version:    1.0
 */

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final FriendService friendService;

    public UserController(UserService userService, FriendService friendService) {
        this.userService = userService;
        this.friendService = friendService;
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

    @GetMapping("/my-info")
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

    @GetMapping("/my-friends")
    public MessageResponse<List<FriendResponse>> getMyFriends() {
        return MessageResponse.<List<FriendResponse>>builder()
                .message("Lấy danh sách bạn bè thành công")
                .data(friendService.getMyFriends())
                .build();
    }

    //Cập nhật thông tin cá nhân (Display_name, Dob, Bio, Status)
    @PutMapping("/my-update")
    public MessageResponse<UserResponse> updateMyInfo(@RequestBody @Valid UserUpdateInfoRequest request) {
        return MessageResponse.<UserResponse>builder()
                .message("Cập nhật thông tin người dùng thành công")
                .data(userService.updateMyInfo(request))
                .build();
    }

    //Get user by userID
    @GetMapping("/{id}")
    public MessageResponse<UserResponse> getUserById(@PathVariable String id) {
        return MessageResponse.<UserResponse>builder()
                .message("Lấy thông tin người dùng thành công")
                .data(userService.getUserById(id))
                .build();
    }

    //Change password
    @PutMapping("/change-password")
    public MessageResponse<UserResponse> changePassword(@RequestBody ChangePasswordRequest request) {
        return MessageResponse.<UserResponse>builder()
                .message("Đổi mật khẩu thành công")
                .data(userService.changePassword(request.getOldPassword(), request.getNewPassword()))
                .build();
    }

    @GetMapping("/search")
    public MessageResponse<UserResponse> searchUserByPhoneOrEmail(String query) {
        return MessageResponse.<UserResponse>builder()
                .message("Tìm thấy người dùng")
                .data(userService.searchUserByPhoneOrEmail(query))
                .build();
    }

    @PutMapping(value = "/my-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MessageResponse<UserResponse> updateMeAvatar( @RequestPart("avatar") MultipartFile avatar) throws IOException {
        UserResponse updatedUser = userService.updateUserAvatar(avatar);
        return MessageResponse.<UserResponse>builder()
                .statusCode(200)
                .success(true)
                .message("Cập nhật ảnh đại diện thành công.")
                .data(updatedUser)
                .build();
    }
}
