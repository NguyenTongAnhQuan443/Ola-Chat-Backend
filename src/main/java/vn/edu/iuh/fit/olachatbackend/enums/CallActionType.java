package vn.edu.iuh.fit.olachatbackend.enums;

import lombok.Getter;

@Getter
public enum CallActionType {
    INVITED("INVITED"), ACCEPTED("ACCEPTED"), REJECTED("REJECTED"), NO_ANSWER("NO_ANSWER");

    private final String value;

    CallActionType(String value) {
        this.value = value;
    }
}
