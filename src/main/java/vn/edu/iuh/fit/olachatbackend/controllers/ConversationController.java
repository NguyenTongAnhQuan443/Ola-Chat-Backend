/*
 * @ (#) ConversationController.java       1.0     14/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.controllers;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 14/02/2025
 * @version:    1.0
 */

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.olachatbackend.dtos.ConversationDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.MessageDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.*;
import vn.edu.iuh.fit.olachatbackend.services.ConversationService;
import vn.edu.iuh.fit.olachatbackend.services.MessageService;
import vn.edu.iuh.fit.olachatbackend.services.UserService;
import vn.edu.iuh.fit.olachatbackend.utils.extractUserIdFromJwt;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
    private final ConversationService conversationService;
    private final MessageService messageService;
    private final UserService userService;

    public ConversationController(ConversationService conversationService, MessageService messageService, UserService userService) {
        this.conversationService = conversationService;
        this.messageService = messageService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ConversationDTO> createConversation(@RequestBody ConversationDTO conversationDTO) {
        return ResponseEntity.ok(conversationService.createConversation(conversationDTO));
    }

    @GetMapping
    public ResponseEntity<MessageResponse<List<ConversationResponse>>> getConversationsByUser() {
        return ResponseEntity.ok(
                new MessageResponse<>(200, "Lấy danh sách cuộc trò chuyện thành công", true, conversationService.getAllConversationsByUser())
        );
    }

    @GetMapping(value = "/{id}/messages", produces = "application/json;charset=UTF-8")
    public ResponseEntity<List<MessageDTO>> getMessagesByConversationId(@PathVariable String id,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "20") int size,
                                                                        @RequestParam(defaultValue = "desc") String sortDirection) {
        List<MessageDTO> messages = messageService.getMessagesByConversationId(id, page, size, sortDirection);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{conversationId}/users")
    public ResponseEntity<List<ParticipantResponse>> getUsersByConversation(@PathVariable String conversationId) {
        List<ParticipantResponse> users = userService.getUsersByConversationId(conversationId);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{conversationId}/media")
    public MessageResponse<List<MediaMessageResponse>> getImagesAndVideos(
            @PathVariable String conversationId,
            @RequestParam(required = false) String senderId
    ) {
        List<MediaMessageResponse> data = messageService.getMediaMessages(conversationId, senderId);
        return MessageResponse.<List<MediaMessageResponse>>builder()
                .message("Lấy media thành công.")
                .data(data)
                .build();
    }

    @GetMapping("/{conversationId}/files")
    public MessageResponse<List<MediaMessageResponse>> getFiles(
            @PathVariable String conversationId,
            @RequestParam(required = false) String senderId
    ) {
        List<MediaMessageResponse> data = messageService.getFileMessages(conversationId, senderId);
        return MessageResponse.<List<MediaMessageResponse>>builder()
                .message("Lấy media thành công.")
                .data(data)
                .build();
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<MessageResponse<Void>> deleteConversationForUser(
            @PathVariable String conversationId,
            @RequestHeader("Authorization") String authHeader
    ) {
        String token = authHeader.replace("Bearer ", "");
        String userId = extractUserIdFromJwt.extractUserIdFromJwt(token);

        conversationService.softDeleteConversation(userId, conversationId);

        return ResponseEntity.ok(
                new MessageResponse<>(200, "Đã xoá cuộc trò chuyện thành công", true, null)
        );
    }

    @GetMapping("/{conversationId}/search")
    public MessageResponse<Page<MessageDTO>> searchMessages(
            @PathVariable("conversationId") String conversationId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String senderId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<MessageDTO> result = messageService.searchMessages(
                conversationId, keyword, senderId, fromDate, toDate, page, size
        );
        return MessageResponse.<Page<MessageDTO>>builder()
                .message("Tìm kiếm tin nhắn thành công.")
                .data(result)
                .build();
    }
}
