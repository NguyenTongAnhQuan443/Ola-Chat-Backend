package vn.edu.iuh.fit.olachatbackend.enums;

import lombok.Getter;

@Getter
public enum MessageType {
    TEXT("TEXT"), MEDIA("MEDIA"), EMOJI("EMOJI"),
    STICKER("STICKER"), VOICE("VOICE"), FILE("FILE"), SYSTEM("SYSTEM");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

}
