/*
 * @ (#) GroupService.java       1.0     28/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.services;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 28/02/2025
 * @version:    1.0
 */

import org.bson.types.ObjectId;
import vn.edu.iuh.fit.olachatbackend.dtos.ConversationDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.AddMemberRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.ChangeBackgroundRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.GroupUpdateRequest;

import java.util.List;

public interface GroupService {
    ConversationDTO createGroup(String name, String avatar, List<String> userIds);
    void updateGroup(ObjectId groupId, GroupUpdateRequest request);
    ConversationDTO getGroupById(ObjectId id);

    void deleteGroup(ObjectId groupId);
    void joinGroup(ObjectId groupId);
    void leaveGroup(ObjectId groupId);

    void addMembers(ObjectId objectId, AddMemberRequest request);

    void removeUserFromGroup(ObjectId groupId, String userId);

    void transferGroupOwner(ObjectId objectId, String newOwnerId);

    void setModerator(ObjectId objectId, String userId);

    void removeModerator(ObjectId objectId, String userId);

    void muteConversation(ObjectId objectId);

    void unmuteConversation(ObjectId objectId);

    void changeBackground(ObjectId groupId, ChangeBackgroundRequest request);
}
