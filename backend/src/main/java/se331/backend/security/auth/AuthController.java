package se331.backend.security.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegistrationRequest request
    ) {
        // เรียก Service และรับ RegistrationResult กลับมา
        RegistrationResult result = service.register(request);

        // ตรวจสอบผลลัพธ์
        if (result.isSuccess()) {
            // ถ้าสำเร็จ: ส่ง 200 OK พร้อม AuthenticationResponse
            return ResponseEntity.ok(result.getAuthResponse());
        } else {
            // ถ้าไม่สำเร็จ: ส่ง 400 Bad Request พร้อมข้อความ Error
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", result.getMessage()));
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(
            @RequestBody AuthRequest request
    ) {
        AuthResponse result = service.authenticate(request);
        return ResponseEntity.ok(result);
    }

    /**
     * ตรวจสอบ Username ว่าซ้ำหรือไม่
     * GET /api/v1/auth/check-username?username=desired_username
     */
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsernameAvailability(@RequestParam("username") String username) {
        boolean taken = service.isUsernameTaken(username);
        // คืนค่า JSON { "isTaken": true/false }
        return ResponseEntity.ok(Map.of("isTaken", taken));
    }

    /**
     * ตรวจสอบ Email ว่าซ้ำหรือไม่
     * GET /api/v1/auth/check-email?email=desired_email@example.com
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmailAvailability(@RequestParam("email") String email) {
        boolean taken = service.isEmailTaken(email);
        // คืนค่า JSON { "isTaken": true/false }
        return ResponseEntity.ok(Map.of("isTaken", taken));
    }
}
