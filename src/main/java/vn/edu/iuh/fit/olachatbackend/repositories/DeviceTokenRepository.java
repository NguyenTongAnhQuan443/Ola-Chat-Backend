/*
 * @ (#) DeviceTokenRepository.java       1.0     06/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.repositories;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 06/04/2025
 * @version:    1.0
 */

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.olachatbackend.entities.DeviceToken;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, UUID> {
    DeviceToken findByUserIdAndToken(String userId, String token);
    DeviceToken findByUserId(String userId);
    DeviceToken findByDeviceId(String deviceId);
    List<DeviceToken> findAllByUserId(String userId);
    List<DeviceToken> findAllByDeviceId(String deviceId);

}
