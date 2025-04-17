package vn.edu.iuh.fit.olachatbackend.enums;

import lombok.Getter;

@Getter
public enum MessageType {
    TEXT("TEXT"), IMAGE("IMAGE"), VIDEO("VIDEO"),
    DOCUMENT("DOCUMENT"), EMOTION("EMOTION"),  VOICE("VOICE");

    private final String value;

    private MessageType(String value) {
        this.value = value;
    }

}
