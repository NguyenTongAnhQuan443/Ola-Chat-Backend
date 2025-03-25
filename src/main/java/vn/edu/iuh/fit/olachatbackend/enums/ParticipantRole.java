package vn.edu.iuh.fit.olachatbackend.enums;

import lombok.Getter;

@Getter
public enum ParticipantRole {
    ADMIN("ADMIN"), MODERATOR("MODERATOR"), MEMBER("MEMBER");

    private final String role;

    ParticipantRole(String role) {
        this.role = role;
    }
}