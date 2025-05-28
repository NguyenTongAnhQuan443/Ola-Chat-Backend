/*
 * @ (#) QRLoginServiceImpl.java       1.0     28/05/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.services.impl;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 28/05/2025
 * @version:    1.0
 */

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.services.QRLoginService;
import vn.edu.iuh.fit.olachatbackend.services.RedisService;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QRLoginServiceImpl implements QRLoginService {

    private final RedisService redisService;
    private final Duration TTL = Duration.ofMinutes(3);

    @Override
    public String createQrToken() {
        String token = UUID.randomUUID().toString();
        redisService.saveQRCodeToken(token, TTL);
        return token;
    }

    @Override
    public boolean isValid(String token) {
        return false;
    }

    @Override
    public void markAsUsed(String token, String userId) {

    }

    @Override
    public boolean isAlreadyUsed(String token) {
        return false;
    }
}
