/*
 * @ (#) FriendServiceImpl.java       1.0     14/02/2025
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
import vn.edu.iuh.fit.olachatbackend.entities.Friend;
import vn.edu.iuh.fit.olachatbackend.repositories.FriendRepository;
import vn.edu.iuh.fit.olachatbackend.services.FriendService;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl implements FriendService {
    private final FriendRepository friendRepository;

    public Friend addFriend(Friend friend) {
        return friendRepository.save(friend);
    }
}
