package se331.backend.security.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se331.backend.entity.UserAuthDTO;
import se331.backend.security.config.JwtService;
import se331.backend.security.token.Token;
import se331.backend.security.token.TokenRepository;
import se331.backend.security.token.TokenType;
import se331.backend.security.user.Role;
import se331.backend.security.user.User;
import se331.backend.security.user.UserRepository;
import se331.backend.util.NewsMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository repository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    private NewsMapper newsMapper;

    @Transactional
    public RegistrationResult register(RegistrationRequest request) {

        // 1. Check if username already exists
        if (repository.findByUsername(request.getUsername()).isPresent()) {
            return RegistrationResult.failure("Username already exists: " + request.getUsername());
        }
        // 2. Check if email already exists
        if (repository.findByEmail(request.getEmail()).isPresent()) {
            return RegistrationResult.failure("Email already exists: " + request.getEmail());
        }

        User user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .profileImage(request.getProfileImage())
                .roles(List.of(Role.ROLE_READER)) // Role เริ่มต้นคือ READER
                .enabled(true)
                .build();

        var savedUser = repository.save(user);

        var jwtToken = jwtService.generateToken(savedUser);
        saveUserToken(savedUser, jwtToken);

        UserAuthDTO userDto = newsMapper.toUserAuthDTO(savedUser);

        // สร้าง AuthenticationResponse สำหรับกรณี Success
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(jwtToken)
                // ไม่ส่ง refreshToken ตอน register
                .user(userDto)
                .build();

        return RegistrationResult.success(authResponse);
    }

    public AuthResponse authenticate(AuthRequest request) {
        // หา user
        String loginKey = request.getIdentifier();
        if (loginKey == null || loginKey.isBlank()) {
            throw new RuntimeException("Username or email is required.");
        }
        User user = repository.findByUsername(loginKey)
                .or(() -> repository.findByEmail(loginKey))
                .orElseThrow(() -> new RuntimeException("User not found."));

        // ตรวจสอบ password ด้วย AuthenticationManager
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        request.getPassword()
                )
        );

        String jwtToken = jwtService.generateToken(user);
        saveUserToken(user, jwtToken);

        UserAuthDTO userDto = newsMapper.toUserAuthDTO(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .user(userDto)
                .build();
    }

    private void saveUserToken(User user, String jwtToken) {
        Token token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    public boolean isUsernameTaken(String username) {
        return repository.findByUsername(username).isPresent();
    }

    public boolean isEmailTaken(String email) {
        return repository.findByEmail(email).isPresent();
    }
}
