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

import com.nimbusds.jose.JOSEException;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.UserRegisterRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UserResponse;
import vn.edu.iuh.fit.olachatbackend.entities.User;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User saveUser(User user);

    Optional<User> getUserById(String id);

    List<User> findAll();

    List<UserResponse> getUsers();

    UserResponse registerUser(UserRegisterRequest request);

    UserResponse getMyInfo();

    void deleteUser(String userId);

    List<UserResponse> getUsersByConversationId(String conversationId);

    UserResponse getMyInfo(String token);

}
