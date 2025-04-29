/*
 * @ (#) GroupController.java       1.0     28/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.controllers;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 28/02/2025
 * @version:    1.0
 */

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.iuh.fit.olachatbackend.dtos.ConversationDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.AddMemberRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.ChangeBackgroundRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.GroupRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.GroupUpdateRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.MessageResponse;
import vn.edu.iuh.fit.olachatbackend.services.GroupService;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<MessageResponse<ConversationDTO>> createGroup(@RequestBody GroupRequest request) {
        try {
            ConversationDTO group = groupService.createGroup(request.getName(), request.getAvatar(), request.getUserIds());
            return ResponseEntity.ok(new MessageResponse<>(200, "Tạo nhóm thành công", true, group));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse<>(400, e.getMessage(), false, null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse<ConversationDTO>> getGroup(@PathVariable String id) {
        ConversationDTO group = groupService.getGroupById(new ObjectId(id));
        return ResponseEntity.ok(new MessageResponse<>(200, "Lấy thông tin nhóm thành công", true, group));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse<Object>> updateGroup(@PathVariable String id,
                                                               @RequestBody GroupUpdateRequest request) {
        groupService.updateGroup(new ObjectId(id), request);
        return ResponseEntity.ok(new MessageResponse<>(200, "Cập nhật nhóm thành công", true));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse<Object>> deleteGroup(@PathVariable String id) {
        groupService.deleteGroup(new ObjectId(id));
        return ResponseEntity.ok(new MessageResponse<>(200, "Đã xóa nhóm thành công", true));
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<MessageResponse<Object>> joinGroup(@PathVariable String id) {
        groupService.joinGroup(new ObjectId(id));
        return ResponseEntity.ok(new MessageResponse<>(200, "Tham gia nhóm thành công", true));
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<MessageResponse<Object>> leaveGroup(@PathVariable String id) {
        groupService.leaveGroup(new ObjectId(id));
        return ResponseEntity.ok(new MessageResponse<>(200, "Rời nhóm thành công", true));
    }

    @PostMapping("/{id}/add-member")
    public ResponseEntity<MessageResponse<Object>> addMembersToGroup(@PathVariable String id,
                                                                     @RequestBody AddMemberRequest request) {
        groupService.addMembers(new ObjectId(id), request);
        return ResponseEntity.ok(new MessageResponse<>(200, "Đã thêm thành viên thành công", true));
    }

    @DeleteMapping("/{id}/remove/{userId}")
    public ResponseEntity<MessageResponse<Object>> removeUserFromGroup(
            @PathVariable String id,
            @PathVariable String userId) {
        groupService.removeUserFromGroup(new ObjectId(id), userId);
        return ResponseEntity.ok(new MessageResponse<>(200, "Đã xóa thành viên khỏi nhóm", true));
    }

    @PostMapping("/{id}/transfer-owner/{newOwnerId}")
    public ResponseEntity<MessageResponse<Object>> transferGroupOwnership(
            @PathVariable String id,
            @PathVariable String newOwnerId) {
        try {
            groupService.transferGroupOwner(new ObjectId(id), newOwnerId);
            return ResponseEntity.ok(new MessageResponse<>(200, "Đã chuyển quyền trưởng nhóm thành công", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse<>(400, e.getMessage(), false, null));
        }
    }

    @PostMapping("/{id}/add-moderator/{userId}")
    public ResponseEntity<MessageResponse<Object>> addDeputyGroupLeader(
            @PathVariable String id,
            @PathVariable String userId) {
        try {
            groupService.setModerator(new ObjectId(id), userId);
            return ResponseEntity.ok(new MessageResponse<>(200, "Đã thêm phó nhóm thành công", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse<>(400, e.getMessage(), false, null));
        }
    }

    @DeleteMapping("/{id}/remove-moderator/{userId}")
    public ResponseEntity<MessageResponse<Object>> removeDeputyGroupLeader(
            @PathVariable String id,
            @PathVariable String userId) {
        groupService.removeModerator(new ObjectId(id), userId);
        return ResponseEntity.ok(new MessageResponse<>(200, "Đã xóa quyền phó nhóm thành công", true));
    }

    @PostMapping("/{groupId}/mute")
    public ResponseEntity<MessageResponse<Object>> muteConversation(@PathVariable String groupId) {
        groupService.muteConversation(new ObjectId(groupId));
        return ResponseEntity.ok(new MessageResponse<>(200, "Đã tắt thông báo nhóm", true));
    }

    @PostMapping("/{groupId}/unmute")
    public ResponseEntity<MessageResponse<Object>> unmuteConversation(@PathVariable String groupId) {
        groupService.unmuteConversation(new ObjectId(groupId));
        return ResponseEntity.ok(new MessageResponse<>(200, "Đã bật lại thông báo nhóm", true));
    }

    @PatchMapping("/{groupId}/change-background")
    public ResponseEntity<MessageResponse<Void>> changeBackground(@PathVariable String groupId,
                                                                  @RequestBody ChangeBackgroundRequest request) {
        groupService.changeBackground(new ObjectId(groupId), request);
        return ResponseEntity.ok(new MessageResponse<>(200, "Thay đổi background nhóm thành công", true));
    }


}