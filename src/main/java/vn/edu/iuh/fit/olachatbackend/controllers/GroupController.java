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
import vn.edu.iuh.fit.olachatbackend.dtos.requests.GroupRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.GroupUpdateRequest;
import vn.edu.iuh.fit.olachatbackend.services.GroupService;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody GroupRequest request) {
        try {
            ConversationDTO group = groupService.createGroup(request.getCreatorId(), request.getName(), request.getAvatar(), request.getUserIds());
            return ResponseEntity.ok(group);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getGroup(@PathVariable String id) {
        return ResponseEntity.ok(groupService.getGroupById(new ObjectId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGroup(@PathVariable String id, @RequestParam String userId,
                                                    @RequestBody GroupUpdateRequest request) {
        groupService.updateGroup(new ObjectId(id), userId, request);
        return ResponseEntity.ok("Cập nhật nhóm thành công!");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable String id, @RequestParam String userId) {
        groupService.deleteGroup(new ObjectId(id), userId);
        return ResponseEntity.ok("Đã xóa nhóm thành công!");
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinGroup(@PathVariable String id, @RequestParam String userId) {
        groupService.joinGroup(new ObjectId(id), userId);
        return ResponseEntity.ok("Tham gia nhóm thành công!");
    }

    @PostMapping("/{id}/leave")
    public ResponseEntity<?> leaveGroup(@PathVariable String id, @RequestParam String userId) {
        groupService.leaveGroup(new ObjectId(id), userId);
        return ResponseEntity.ok("Rời nhóm thành công!");
    }

    @PostMapping("/{id}/add-member")
    public ResponseEntity<?> addMembersToGroup(@PathVariable String id,
                                                          @RequestParam String ownerId,
                                                          @RequestBody AddMemberRequest request) {
        groupService.addMembers(new ObjectId(id), ownerId, request);
        return ResponseEntity.ok("Đã thêm thành viên thành công!");
    }

    @DeleteMapping("/{id}/remove/{userId}")
    public ResponseEntity<?> removeUserFromGroup(
            @PathVariable String id,
            @PathVariable String userId,
            @RequestHeader("requesterId") String requesterId) {
        groupService.removeUserFromGroup(new ObjectId(id), userId, requesterId);
        return ResponseEntity.ok("Đã xóa thành viên khỏi nhóm");
    }

}
