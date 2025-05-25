package vn.edu.iuh.fit.olachatbackend.dtos.responses;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.edu.iuh.fit.olachatbackend.enums.AuthProvider;
import vn.edu.iuh.fit.olachatbackend.enums.Role;
import vn.edu.iuh.fit.olachatbackend.enums.UserStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserResponse {
    String userId;
    String email;
    String username;
    String displayName;
    String nickname;
    String avatar;
    String bio;
    Date dob;
    UserStatus status;
    AuthProvider authProvider;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    Role role;
}