package vn.edu.iuh.fit.olachatbackend.enums;

import lombok.Getter;

@Getter

public enum NotificationType {
    FRIEND_REQUEST("FRIEND_REQUEST"),
    MESSAGE("MESSAGE"),
    MENTION("MENTION"),
    GROUP("GROUP"),
    SYSTEM("SYSTEM"),
    POST_LIKE("POST_LIKE"),
    POST_COMMENT("POST_COMMENT"),
    POST_SHARE("POST_SHARE");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }
}