/*
 * @ (#) FriendRequestService.java       1.0     14/02/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.services;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 14/02/2025
 * @version:    1.0
 */

import vn.edu.iuh.fit.olachatbackend.dtos.FriendRequestDTO;
import vn.edu.iuh.fit.olachatbackend.dtos.responses.FriendRequestResponse;

import java.util.List;

public interface FriendRequestService {
    public FriendRequestDTO sendFriendRequest(FriendRequestDTO friendRequestDTO);
    public List<FriendRequestResponse> getReceivedFriendRequests();
    public List<FriendRequestResponse> getSentFriendRequests();
    public void acceptFriendRequest(String requestId);
    public void rejectFriendRequest(String requestId);
    void unfriend(String userId, String friendId);
    public void cancelSentRequest(String receiverId);
}
