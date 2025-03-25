/*
 * @ (#) AuthProvider.java       1.0     23/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.olachatbackend.enums;
/*
 * @description:
 * @author: Nguyen Thanh Nhut
 * @date: 23/03/2025
 * @version:    1.0
 */

import lombok.Getter;

@Getter
public enum AuthProvider {
    GOOGLE("GOOGLE"),
    FACEBOOK("FACEBOOK"),
    LOCAL("LOCAL");

    private final String value;

    private AuthProvider(String value) {
        this.value = value;
    }
}
