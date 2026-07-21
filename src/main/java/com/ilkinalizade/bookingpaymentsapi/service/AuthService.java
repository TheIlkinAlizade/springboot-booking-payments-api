package com.ilkinalizade.bookingpaymentsapi.service;

import com.ilkinalizade.bookingpaymentsapi.dto.request.LoginRequest;
import com.ilkinalizade.bookingpaymentsapi.dto.request.RegisterRequest;
import com.ilkinalizade.bookingpaymentsapi.dto.response.AuthResponse;
import com.ilkinalizade.bookingpaymentsapi.entity.Role;
import com.ilkinalizade.bookingpaymentsapi.entity.User;
import com.ilkinalizade.bookingpaymentsapi.repository.UserRepository;
import com.ilkinalizade.bookingpaymentsapi.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(Role.USER) // registration always creates a regular user, never admin
                .build();

        userRepository.save(user);

        return authenticate(request.getEmail(), request.getPassword());
    }

    public AuthResponse login(LoginRequest request) {
        return authenticate(request.getEmail(), request.getPassword());
    }

    private AuthResponse authenticate(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        String token = jwtTokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User vanished after authentication"));

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }
}