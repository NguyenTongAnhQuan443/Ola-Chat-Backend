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
import vn.edu.iuh.fit.olachatbackend.entities.FriendRequest;
import vn.edu.iuh.fit.olachatbackend.entities.User;
import vn.edu.iuh.fit.olachatbackend.enums.RequestStatus;
import vn.edu.iuh.fit.olachatbackend.exceptions.ConflicException;
import vn.edu.iuh.fit.olachatbackend.exceptions.NotFoundException;
import vn.edu.iuh.fit.olachatbackend.repositories.FriendRequestRepository;
import vn.edu.iuh.fit.olachatbackend.repositories.UserRepository;
import vn.edu.iuh.fit.olachatbackend.services.FriendRequestService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendRequestServiceImpl implements FriendRequestService {
    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

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
}
