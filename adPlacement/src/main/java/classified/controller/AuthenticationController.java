package classified.controller;

import classified.dto.AuthResponse;
import classified.dto.LoginRequest;
import classified.dto.UserRegistrationRequest;
import classified.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import classified.security.UserDetailsImpl;
import classified.service.JwtService;
import classified.service.UserService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    /// Возвращает JWT
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        jwtService.isTokenValid(token, userDetails);
        return ResponseEntity.ok(new AuthResponse(token, userDetails.getId(), userDetails.getUsername()));
    }

    /// Создает пользователя и возвращает JWT
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserRegistrationRequest userRegistrationRequest) {
        UserResponse user = userService.register(userRegistrationRequest);
        return ResponseEntity.ok(user);
    }
}
