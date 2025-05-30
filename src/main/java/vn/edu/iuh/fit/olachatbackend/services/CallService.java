/*
 * @ (#) CallService.java       1.0     30/05/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.services;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 30/05/2025
 * @version:    1.0
 */

import vn.edu.iuh.fit.olachatbackend.dtos.requests.CallNotificationRequest;

public interface CallService {
    void inviteCall(CallNotificationRequest request);
    void acceptCall(CallNotificationRequest request);
    void rejectCall(CallNotificationRequest request);
    void cancelCall(CallNotificationRequest request);
}
