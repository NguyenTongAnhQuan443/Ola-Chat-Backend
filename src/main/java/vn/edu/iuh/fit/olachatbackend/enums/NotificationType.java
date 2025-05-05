package vn.edu.iuh.fit.olachatbackend.enums;

import lombok.Getter;

@Getter
public enum NotificationType {
    FRIEND_REQUEST("FRIEND_REQUEST"),
    MESSAGE("MESSAGE"),
    MENTION("MENTION"),
    ALERT("ALERT"),
    PROMOTION("PROMOTION"),
    SYSTEM("SYSTEM");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }
}