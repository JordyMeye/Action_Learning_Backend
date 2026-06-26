package fr.epita.controller;

import fr.epita.dto.Request.ChangePasswordRequest;
import fr.epita.dto.Request.LoginRequest;
import fr.epita.dto.Request.RegisterRequest;
import fr.epita.dto.Response.AuthResponse;
import fr.epita.dto.Response.UserProfileResponse;
import fr.epita.model.AppUser;
import fr.epita.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(@AuthenticationPrincipal AppUser currentUser) {
        return ResponseEntity.ok(UserProfileResponse.builder()
                .id(currentUser.getId())
                .firstName(currentUser.getFirstName())
                .surname(currentUser.getSurname())
                .email(currentUser.getEmail())
                .role(currentUser.getRole().name())
                .universityId(currentUser.getUniversityId())
                .build());
    }

    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal AppUser currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(currentUser.getEmail(), request);
        return ResponseEntity.ok().build();
    }
}
