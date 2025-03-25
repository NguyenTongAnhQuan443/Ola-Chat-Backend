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
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.entities.FriendRequest;
import vn.edu.iuh.fit.olachatbackend.repositories.FriendRequestRepository;
import vn.edu.iuh.fit.olachatbackend.services.FriendRequestService;

@Service
@RequiredArgsConstructor
public class FriendRequestServiceImpl implements FriendRequestService {
    private final FriendRequestRepository friendRequestRepository;

    public FriendRequest sendRequest(FriendRequest request) {
        return friendRequestRepository.save(request);
    }
}
