package vn.edu.iuh.fit.olachatbackend.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("ACTIVE"), INACTIVE("INACTIVE"), DELETED("DELETED");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

}
