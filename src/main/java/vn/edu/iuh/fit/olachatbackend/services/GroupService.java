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
import vn.edu.iuh.fit.olachatbackend.dtos.requests.GroupUpdateRequest;

import java.util.List;

public interface GroupService {
    ConversationDTO createGroup(String creatorId, String name, String avatar, List<String> userIds);
    ConversationDTO updateGroup(ObjectId groupId, String userId, GroupUpdateRequest request);
    ConversationDTO getGroupById(ObjectId id);
}
