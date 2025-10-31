package se331.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se331.backend.security.user.Role;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthDTO {
    Integer id;
    String username;
    String firstname;
    String lastname;
    String email;
    String profileImage;
    List<Role> roles;
}