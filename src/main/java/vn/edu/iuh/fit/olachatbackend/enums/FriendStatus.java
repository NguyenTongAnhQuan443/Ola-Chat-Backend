package vn.edu.iuh.fit.olachatbackend.enums;

import lombok.Getter;

@Getter
public enum FriendStatus {
    ACTIVE("ACTIVE"), BLOCKED("BLOCKED");

    private final String value;

    private FriendStatus(String value) {
        this.value = value;
    }
}
