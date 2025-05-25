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

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.IntrospectRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.SetContactAliasRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.UserRegisterRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.UserUpdateInfoRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.IntrospectResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.ParticipantResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UserResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.UserSearchResponse;
import vn.edu.iuh.fit.olachatbackend.entities.FriendRequest;
import vn.edu.iuh.fit.olachatbackend.entities.Participant;
import vn.edu.iuh.fit.olachatbackend.entities.User;
import vn.edu.iuh.fit.olachatbackend.entities.ContactAlias;
import vn.edu.iuh.fit.olachatbackend.enums.LoginHistoryStatus;
import vn.edu.iuh.fit.olachatbackend.enums.RequestStatus;
import vn.edu.iuh.fit.olachatbackend.exceptions.BadRequestException;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.exceptions.UnauthorizedException;
import vn.edu.iuh.fit.olachatbackend.mappers.ParticipantMapper;
import vn.edu.iuh.fit.olachatbackend.mappers.UserMapper;
import vn.edu.iuh.fit.olachatbackend.repositories.*;
import vn.edu.iuh.fit.olachatbackend.services.*;
import vn.edu.iuh.fit.olachatbackend.utils.OtpUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ParticipantRepository participantRepository;
    private final AuthenticationService authenticationService;
    private final RedisService redisService;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;
    private final ParticipantMapper participantMapper;
    private final LoginHistoryRepository loginHistoryRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final ContactAliasRepository userNicknameRepository;

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public UserResponse getUserById(String id) {
        return userRepository.findById(id)
                .map(userMapper::toUserResponse)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng với id: " + id));
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
        String email = request.getEmail();

        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Số điện thoại đăng ký đã tồn tại");
        }

        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email đã được sử dụng");
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
    public List<ParticipantResponse> getUsersByConversationId(String conversationId) {
        List<Participant> participants = participantRepository.findParticipantByConversationId(new ObjectId(conversationId));

        List<String> userIds = participants.stream()
                .map(Participant::getUserId)
                .toList();

        System.out.println(userIds);

        List<Participant> parts = participantRepository.findByConversationIdAndUserIdIn(
                new ObjectId(conversationId),
                userIds
        );

        return parts.stream().map(p -> {
            ParticipantResponse pResponse = participantMapper.toParticipantResponse(p);
            User info = userRepository.findById(p.getUserId()).get();
            pResponse.setDisplayName(info.getDisplayName());
            pResponse.setAvatar(info.getAvatar());
            boolean status = loginHistoryRepository
                    .findTopByUserIdAndStatusOrderByLoginTimeDesc(p.getUserId(), LoginHistoryStatus.ONLINE)
                    .isPresent();
            pResponse.setStatus(status? LoginHistoryStatus.ONLINE : LoginHistoryStatus.OFFLINE);
            return pResponse;
        }).toList();
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

    @Override
    public UserResponse updateMyInfo(UserUpdateInfoRequest request) {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        if (request.getDob() != null) {
            user.setDob(request.getDob().atStartOfDay());
        }

        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }

        User updatedUser = userRepository.save(user);

        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    public UserResponse changePassword(String oldPassword, String newPassword) {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));

        // Giới hạn 1 giờ/lần đổi mật khẩu
        String redisKey = "PASSWORD_CHANGE_LIMIT:" + user.getId();
        Long lastChanged = redisService.getLong(redisKey);
        long now = System.currentTimeMillis();

        if (lastChanged != null && (now - lastChanged) < 3600_000) {
            throw new UnauthorizedException("Bạn chỉ có thể đổi mật khẩu mỗi 1 giờ. Vui lòng thử lại sau.");
        }

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new UnauthorizedException("Mật khẩu cũ không chính xác");
        }

        // Đổi mật khẩu và cập nhật
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(user);

        // Cập nhật mốc thời gian đổi mật khẩu gần nhất vào Redis
        redisService.setLong(redisKey, now, 1, TimeUnit.HOURS);

        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    public UserSearchResponse searchUserByPhoneOrEmail(String query) {
        User currentUser = getCurrentUser();

        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query không được để trống");
        }

        Optional<User> userOptional = query.contains("@")
                ? userRepository.findByEmail(query)
                : userRepository.findByUsername(query);

        if (userOptional.isEmpty()) {
            throw new NotFoundException("Không tìm thấy người dùng");
        }

        User targetUser = userOptional.get();

        int actionCode;

        if (targetUser.getId().equals(currentUser.getId())) {
            actionCode = 0; // NONE
        } else if (friendRequestRepository.areFriends(currentUser, targetUser)) {
            actionCode = 4; // UNFRIEND
        } else {
            Optional<FriendRequest> sent = friendRequestRepository.findBySenderAndReceiver(currentUser, targetUser);
            Optional<FriendRequest> received = friendRequestRepository.findBySenderAndReceiver(targetUser, currentUser);

            if (sent.isPresent() && sent.get().getStatus() == RequestStatus.PENDING) {
                actionCode = 2; // CANCEL_REQUEST
            } else if (received.isPresent() && received.get().getStatus() == RequestStatus.PENDING) {
                actionCode = 3; // ACCEPT_REQUEST
            } else {
                actionCode = 1; // SEND_REQUEST
            }
        }


        return UserSearchResponse.builder()
                .userId(String.valueOf(targetUser.getId()))
                .email(targetUser.getEmail())
                .displayName(targetUser.getDisplayName())
                .nickname(targetUser.getNickname())
                .avatar(targetUser.getAvatar())
                .bio(targetUser.getBio())
                .dob(targetUser.getDob())
                .friendAction(actionCode)
                .build();
    }


    @Override
    public UserResponse updateUserAvatar( MultipartFile avatar) throws IOException {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));

        String avatarUrl = cloudinaryService.uploadImage(avatar);
        user.setAvatar(avatarUrl);

        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }


    @Override
    public void requestEmailUpdate(String newEmail) {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));

        if (newEmail.equalsIgnoreCase(user.getEmail())) {
            throw new BadRequestException("Email mới không được trùng với email hiện tại");
        }

        String userId = user.getId();
        String otpCode = OtpUtils.generateOtp();

        redisService.saveEmailUpdateOtp(userId, otpCode, newEmail);
        emailService.sendVerifyNewEmail(newEmail, otpCode);
    }


    @Override
    public UserResponse verifyAndUpdateEmail(String otpInput) {
        var context = SecurityContextHolder.getContext();
        String currentUsername = context.getAuthentication().getName();

        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng."));

        String userId = user.getId();

        String storedOtp = redisService.getEmailUpdateOtp(userId);
        String newEmail = redisService.getEmailUpdateNewEmail(userId);

        if (storedOtp == null || newEmail == null) {
            throw new BadRequestException("OTP đã hết hạn hoặc không hợp lệ.");
        }

        if (!storedOtp.equals(otpInput)) {
            throw new BadRequestException("Mã OTP không chính xác.");
        }

        user.setEmail(newEmail);
        User savedUser = userRepository.save(user);

        redisService.deleteEmailUpdateOtp(userId);

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public void setContactAlias(SetContactAliasRequest request) {
        // Check ì user exists
        User owner = getCurrentUser();
        User target = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng cần đặt tên gợi nhớ."));

        // Set nickname
        ContactAlias userNickname = userNicknameRepository.findByOwnerIdAndTargetId(owner.getId(), target.getId())
                .map(existing -> {
                    existing.setAliasName(request.getContactAlias());
                    return existing;
                })
                .orElse(ContactAlias.builder()
                        .owner(owner)
                        .target(target)
                        .aliasName(request.getContactAlias())
                        .build());

        userNicknameRepository.save(userNickname);
    }

    private User getCurrentUser() {
        // Check user
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        return userRepository.findByUsername(name)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
    }

}
