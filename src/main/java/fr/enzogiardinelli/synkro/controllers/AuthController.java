package fr.enzogiardinelli.synkro.controllers;

import fr.enzogiardinelli.synkro.dtos.user.AuthResponse;
import fr.enzogiardinelli.synkro.dtos.user.LoginRequest;
import fr.enzogiardinelli.synkro.dtos.user.RegisterRequest;
import fr.enzogiardinelli.synkro.entities.users.RefreshToken;
import fr.enzogiardinelli.synkro.entities.users.User;
import fr.enzogiardinelli.synkro.entities.users.UserRole;
import fr.enzogiardinelli.synkro.repositories.UserRepository;
import fr.enzogiardinelli.synkro.security.JwtSecurity;
import fr.enzogiardinelli.synkro.services.RefreshTokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtSecurity jwtSecurity;
    private final RefreshTokenService refreshTokenService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtSecurity jwtSecurity, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtSecurity = jwtSecurity;
        this.refreshTokenService = refreshTokenService;
    }

    private ResponseCookie generateRefreshTokenResponseCookie(User user) {
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());
        return ResponseCookie.from("refreshToken", refreshToken.getToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge((long) refreshTokenService.getRefreshTokenExpiration() * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "This email is already in use"));
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(hashedPassword);

        user.setRole(UserRole.ROLE_USER);
        userRepository.save(user);

        String accessToken = jwtSecurity.generateToken(user.getEmail());
        ResponseCookie springCookie = generateRefreshTokenResponseCookie(user);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, springCookie.toString())
                .body(new AuthResponse(accessToken));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String accessToken = jwtSecurity.generateToken(user.getEmail());
        ResponseCookie springCookie = generateRefreshTokenResponseCookie(user);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, springCookie.toString())
                .body(new AuthResponse(accessToken));
    }

    @PostMapping("logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken) {
        if (refreshToken != null) {
            refreshTokenService.findByToken(refreshToken).ifPresent(token -> {
                refreshTokenService.deleteByUserId(token.getUser());
            });
        }

        ResponseCookie cleanCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = "refreshToken") String requestRefreshToken) {
        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                   String accessToken = jwtSecurity.generateToken(user.getEmail());
                   return ResponseEntity.ok(new AuthResponse(accessToken));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token not found or invalid"));
    }
}
