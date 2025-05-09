/*
 * @ (#) UserService.java       1.0     14/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.services;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 14/02/2025
 * @version:    1.0
 */

import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.UserRegisterRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.UserUpdateInfoRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.ParticipantResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UserResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UserSearchResponse;
import vn.edu.iuh.fit.olachatbackend.entities.User;

import java.io.IOException;
import java.util.List;

public interface UserService {
    User saveUser(User user);

    //Get user by userID
    UserResponse getUserById(String id);

    List<User> findAll();

    List<UserResponse> getUsers();

    UserResponse registerUser(UserRegisterRequest request);

    UserResponse getMyInfo();

    void deleteUser(String userId);

    List<ParticipantResponse> getUsersByConversationId(String conversationId);

    UserResponse getMyInfo(String token);

    //Cập nhật thông tin cá nhân (Display_name, Dob, Bio, Status)
    UserResponse updateMyInfo(UserUpdateInfoRequest request);

    //Change password
    UserResponse changePassword(String oldPassword, String newPassword);
    UserSearchResponse searchUserByPhoneOrEmail(String query);

    public UserResponse updateUserAvatar( MultipartFile avatar) throws IOException;

    public void requestEmailUpdate( String newEmail);
    public UserResponse verifyAndUpdateEmail(String otpInput);

}
