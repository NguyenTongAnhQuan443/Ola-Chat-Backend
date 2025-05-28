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

import jakarta.servlet.http.HttpServletRequest;
import vn.edu.iuh.fit.olachatbackend.dtos.QrLoginSession;
import vn.edu.iuh.fit.olachatbackend.dtos.requests.QrSessionRequest;

public interface QRLoginService {
    String createQrToken(QrSessionRequest request, HttpServletRequest httpRequest);
    QrLoginSession scanQrAndGetInfo(String sessionId);
    void confirm(String qrToken);
}
