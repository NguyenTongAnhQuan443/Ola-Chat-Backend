package vn.edu.iuh.fit.olachatbackend.enums;

import lombok.Getter;

@Getter
public enum CallType {
    VIDEO("INVITED"), VOICE("VOICE");

    private final String value;

    CallType(String value) {
        this.value = value;
    }
}
