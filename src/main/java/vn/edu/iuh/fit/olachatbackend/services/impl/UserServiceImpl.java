/*
 * @ (#) UserServiceImpl.java       1.0     14/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.services.impl;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 14/02/2025
 * @version:    1.0
 */

import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.IntrospectRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.UserRegisterRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.IntrospectResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UserResponse;
import vn.edu.iuh.fit.olachatbackend.entities.Participant;
import vn.edu.iuh.fit.olachatbackend.entities.User;
import vn.edu.iuh.fit.olachatbackend.enums.AuthProvider;
import vn.edu.iuh.fit.olachatbackend.enums.Role;
import vn.edu.iuh.fit.olachatbackend.enums.UserStatus;
import vn.edu.iuh.fit.olachatbackend.exceptions.InternalServerErrorException;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.exceptions.UnauthorizedException;
import vn.edu.iuh.fit.olachatbackend.mappers.UserMapper;
import vn.edu.iuh.fit.olachatbackend.repositories.ParticipantRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.UserRepository;
import vn.edu.iuh.fit.olachatbackend.services.AuthenticationService;
import vn.edu.iuh.fit.olachatbackend.services.UserService;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ParticipantRepository participantRepository;
    private final AuthenticationService authenticationService;

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<UserResponse> getUsers() {
        UserResponse userResponse = new UserResponse();
        userResponse = userMapper.toUserResponse(userRepository.findAll().get(0));
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    public UserResponse registerUser(UserRegisterRequest request){
        String username = request.getUsername();
        if (userRepository.existsByUsername(username)) {
            throw new InternalServerErrorException("Tên đăng nhập đã tồn tại");
        }
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userMapper.toUserResponse(userRepository.save(user));
    }


    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));

        return userMapper.toUserResponse(user);
    }


    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserResponse> getUsersByConversationId(String conversationId) {
        List<Participant> participants = participantRepository.findParticipantByConversationId(new ObjectId(conversationId));

        List<String> userIds = participants.stream()
                .map(Participant::getUserId)
                .toList();

        List<User> users = userRepository.findAllById(userIds);

        return users.stream().map(userMapper::toUserResponse).toList();
    }

    @Override
    public UserResponse getMyInfo(String token)  {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new UnauthorizedException("Token không hợp lệ!");
        }

        try {
            token = token.substring(7);

            IntrospectResponse response = authenticationService.introspect(new IntrospectRequest(token));

            if (!response.isValid()) {
                throw new UnauthorizedException("Token không hợp lệ hoặc đã hết hạn!");
            }

            String email = response.getUserId(); // Kiểm tra lại nếu email có trong response

            return userRepository.findByEmail(email)
                    .map(userMapper::toUserResponse)
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng với email: " + email));

        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("Lỗi xử lý xác thực token: " + e.getMessage());
        }
    }

}
