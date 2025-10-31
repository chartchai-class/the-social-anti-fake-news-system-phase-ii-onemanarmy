package se331.backend.rest.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se331.backend.security.user.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}/promote")
    public ResponseEntity<?> promoteUser(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(userService.promoteUserToMember(id));
    }

    @PutMapping("/{id}/demote")
    public ResponseEntity<?> demoteUser(@PathVariable("id") Integer id) {
        try {
            return ResponseEntity.ok(userService.demoteUserToReader(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }
    }
}