package vn.edu.iuh.fit.olachatbackend.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.olachatbackend.dtos.FriendRequestDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.FriendRequestResponse;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MessageResponse;
import vn.edu.iuh.fit.olachatbackend.services.FriendRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendRequestService friendRequestService;

    @PostMapping("/send-request")
    public MessageResponse<FriendRequestDTO> sendFriendRequest(@RequestBody FriendRequestDTO request) {
        FriendRequestDTO data = friendRequestService.sendFriendRequest(request);
        return MessageResponse.<FriendRequestDTO>builder()
                .message("Đã gửi lời mời kết bạn.")
                .data(data)
                .build();
    }

    @GetMapping("/requests/received")
    public MessageResponse<List<FriendRequestResponse>> getReceivedFriendRequests() {
        return MessageResponse.<List<FriendRequestResponse>>builder()
                .message("Danh sách lời mời kết bạn đã nhận.")
                .data(friendRequestService.getReceivedFriendRequests())
                .build();
    }

    @GetMapping("/requests/sent")
    public MessageResponse<List<FriendRequestResponse>> getSentFriendRequests() {
        return MessageResponse.<List<FriendRequestResponse>>builder()
                .message("Danh sách lời mời kết bạn đã gửi.")
                .data(friendRequestService.getSentFriendRequests())
                .build();
    }

    @PutMapping("/{requestId}/accept")
    public MessageResponse<Void> acceptFriendRequest(@PathVariable String requestId) {
        friendRequestService.acceptFriendRequest(requestId);
        return MessageResponse.<Void>builder()
                .message("Đã chấp nhận lời mời kết bạn.")
                .build();
    }

    @PutMapping("/{requestId}/reject")
    public MessageResponse<Void> rejectFriendRequest(@PathVariable String requestId) {
        friendRequestService.rejectFriendRequest(requestId);
        return MessageResponse.<Void>builder()
                .message("Đã từ chối lời mời kết bạn.")
                .build();
    }

    @DeleteMapping("/unfriend")
    public MessageResponse<Void> unfriend(@RequestParam String userId,
                                          @RequestParam String friendId) {
        friendRequestService.unfriend(userId, friendId);
        return MessageResponse.<Void>builder()
                .message("Hủy kết bạn thành công!")
                .build();
    }

    @DeleteMapping("/requests/{receiverId}/cancel")
    public MessageResponse<Void> cancelFriendRequest(@PathVariable String receiverId) {
        friendRequestService.cancelSentRequest(receiverId);
        return MessageResponse.<Void>builder()
                .message("Đã hủy lời mời kết bạn.")
                .build();
    }



}
