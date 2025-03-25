package vn.edu.iuh.fit.olachatbackend.enums;

import lombok.Getter;

@Getter
public enum ConversationType {
    PRIVATE("PRIVATE"), GROUP("GROUP");

    private final String value;

    private ConversationType(String value) {
        this.value = value;
    }
}

