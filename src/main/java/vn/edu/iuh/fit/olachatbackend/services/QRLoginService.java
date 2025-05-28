/*
 * @ (#) QRLoginService.java       1.0     28/05/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.services;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 28/05/2025
 * @version:    1.0
 */

public interface QRLoginService {
    String createQrToken();
    boolean isValid(String token);
    void markAsUsed(String token, String userId);
    boolean isAlreadyUsed(String token);
}
