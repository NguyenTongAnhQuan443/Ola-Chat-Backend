package vn.edu.iuh.fit.olachatbackend.enums;

import lombok.Getter;

@Getter
public enum RequestStatus {
    PENDING("PENDING"), ACCEPTED("ACCEPTED"), REJECTED("REJECTED");

    private final String value;

    private RequestStatus(String value) {
        this.value = value;
    }
}
