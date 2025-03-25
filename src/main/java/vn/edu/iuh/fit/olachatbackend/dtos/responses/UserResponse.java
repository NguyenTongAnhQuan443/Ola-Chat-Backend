package vn.edu.iuh.fit.olachatbackend.dtos.responses;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.edu.iuh.fit.olachatbackend.enums.Role;
import vn.edu.iuh.fit.olachatbackend.enums.UserStatus;

import java.time.LocalDate;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class UserResponse {
    String userId;
    String email;
    String phone;
    String username;
    String displayName;
    String avatar;
    Date dob;
    UserStatus status;

    Role role;
}