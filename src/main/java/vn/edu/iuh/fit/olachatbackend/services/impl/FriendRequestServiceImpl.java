/*
 * @ (#) FriendRequestServiceImpl.java       1.0     14/02/2025
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.dtos.FriendRequestDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.FriendRequestResponse;
import vn.edu.iuh.fit.olachatbackend.entities.DeviceToken;
import vn.edu.iuh.fit.olachatbackend.entities.Friend;
import vn.edu.iuh.fit.olachatbackend.entities.FriendRequest;
import vn.edu.iuh.fit.olachatbackend.entities.User;
import vn.edu.iuh.fit.olachatbackend.enums.FriendStatus;
import vn.edu.iuh.fit.olachatbackend.enums.RequestStatus;
import vn.edu.iuh.fit.olachatbackend.exceptions.*;
import vn.edu.iuh.fit.olachatbackend.repositories.DeviceTokenRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.FriendRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.FriendRequestRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.UserRepository;
import vn.edu.iuh.fit.olachatbackend.services.FriendRequestService;
import vn.edu.iuh.fit.olachatbackend.services.NotificationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendRequestServiceImpl implements FriendRequestService {
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final DeviceTokenRepository deviceTokenRepository;
    private final NotificationService notificationService;

    @Override
    public FriendRequestDTO sendFriendRequest(FriendRequestDTO friendRequestDTO) {

        String receiverId = friendRequestDTO.getReceiverId();
        String senderId = friendRequestDTO.getSenderId();

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new NotFoundException("Người gửi không tồn tại."));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new NotFoundException("Người nhận không tồn tại."));

        if (friendRequestRepository.existsBySenderAndReceiver(sender, receiver)) {
            throw new ConflicException("Lời mời đã được gửi trước đó.");
        }

        if (friendRequestRepository.areFriends(sender, receiver)) {
            throw new ConflicException("Hai người đã là bạn bè.");
        }

        try {
            DeviceToken deviceToken = deviceTokenRepository.findByUserId(receiverId);
            if (deviceToken != null) {
                notificationService.sendFriendRequestNotification(senderId, receiverId, deviceToken.getToken());
            } else {
                throw new NotFoundException("Không tìm thấy token cho user: " + receiverId);
            }
        } catch (Exception e) {
            throw new InternalServerErrorException("Lỗi khi gửi thông báo");
        }

        FriendRequest friendRequest = new FriendRequest();
        friendRequest.setSender(sender);
        friendRequest.setReceiver(receiver);
        friendRequest.setStatus(RequestStatus.PENDING);

        FriendRequest rs =  friendRequestRepository.save(friendRequest);

        return FriendRequestDTO.builder()
                .senderId(rs.getSender().getId())
                .receiverId(rs.getReceiver().getId())
                .build();

    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng này"));
    }

    // Lấy danh sách lời mời đã nhận
    @Override
    public List<FriendRequestResponse> getReceivedFriendRequests() {
        User currentUser = getCurrentUser();
        List<FriendRequest> requests = friendRequestRepository.findByReceiverAndStatus(currentUser, RequestStatus.PENDING);

        if (requests.isEmpty()) {
            throw new NotFoundException("Bạn chưa nhận được lời mời kết bạn.");
        }

        return requests.stream()
                .map(req -> new FriendRequestResponse(
                        req.getId(),
                        req.getReceiver().getId(),
                        req.getSender().getDisplayName(),
                        req.getSender().getAvatar()
                ))
                .collect(Collectors.toList());
    }

    public List<FriendRequestResponse> getSentFriendRequests() {
        User currentUser = getCurrentUser();
        List<FriendRequest> requests = friendRequestRepository.findBySenderAndStatus(currentUser, RequestStatus.PENDING);

        if (requests.isEmpty()) {
            throw new NotFoundException("Bạn chưa gửi bất kì lời mời kết nào.");
        }

        return requests.stream()
                .map(req -> new FriendRequestResponse(
                        req.getId(),
                        req.getReceiver().getId(),
                        req.getReceiver().getDisplayName(),
                        req.getReceiver().getAvatar()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public void acceptFriendRequest(String requestId) {
        User receiver  = getCurrentUser();

        // Tìm lời mời kết bạn
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow( () -> new NotFoundException("Không tìm thấy lời mời kết bạn."));

        // Kiểm tra xem người dùng có phải là người nhận không
        if (!friendRequest.getReceiver().equals(receiver)) {
            throw new UnauthorizedException("Bạn không có quyền chấp nhận lời mời này!");
        }

        if (!friendRequest.getStatus().equals(RequestStatus.PENDING)) {
            throw new BadRequestException("Lời mời kết bạn này đã được xử lý trước đó!");
        }

        // Cập nhật trạng thái lời mời
        friendRequest.setStatus(RequestStatus.ACCEPTED);
        friendRequest.setResponseAt(LocalDateTime.now());

        Friend friend = new Friend();
        friend.setUser(friendRequest.getSender());
        friend.setFriend(receiver);
        friend.setStatus(FriendStatus.ACTIVE);

        friendRequestRepository.save(friendRequest);
        friendRepository.save(friend);
    }

    @Override
    public void rejectFriendRequest(String requestId) {
        User receiver  = getCurrentUser();

        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow( () -> new NotFoundException("Không tìm thấy lời mời kết bạn."));

        if (!friendRequest.getReceiver().equals(receiver)) {
            throw new UnauthorizedException("Bạn không có quyền chấp nhận lời mời này!");
        }

        if (!friendRequest.getStatus().equals(RequestStatus.PENDING)) {
            throw new BadRequestException("Lời mời kết bạn này đã được xử lý trước đó!");
        }

        friendRequest.setStatus(RequestStatus.REJECTED);
        friendRequest.setResponseAt(LocalDateTime.now());

        friendRequestRepository.save(friendRequest);
    }

    @Override
    public void registerDevice(String userId, String token) {
        DeviceToken deviceToken = deviceTokenRepository.findByUserId(userId);
        if (deviceToken == null) {
            deviceToken = new DeviceToken();
            deviceToken.setUserId(userId);
        }
        deviceToken.setToken(token);
        deviceTokenRepository.save(deviceToken);

    }
}
