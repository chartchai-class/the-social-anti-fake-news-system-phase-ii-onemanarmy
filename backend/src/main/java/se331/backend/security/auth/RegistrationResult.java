package se331.backend.security.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResult {
    private boolean success;
    private String message;
    private AuthResponse authResponse;

    public static RegistrationResult success(AuthResponse response) {
        return RegistrationResult.builder()
                .success(true)
                .message("Registration successful!")
                .authResponse(response)
                .build();
    }

    public static RegistrationResult failure(String message) {
        return RegistrationResult.builder()
                .success(false)
                .message(message)
                .authResponse(null)
                .build();
    }
}