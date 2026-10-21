
package com.clearanceflow.backend.service;

import com.clearanceflow.backend.dto.RegisterRequestDTO;
import com.clearanceflow.backend.dto.LoginRequestDTO;
import com.clearanceflow.backend.dto.AuthResponseDTO;
import com.clearanceflow.backend.entity.User;
import com.clearanceflow.backend.enums.Role;
import com.clearanceflow.backend.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // CONSTRUCTOR
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // REGISTER USER
    public void register(RegisterRequestDTO request) {

        // Check whether email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // Store BCrypt hash instead of plain-text password
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        // Public registration always creates an APPLICANT
        user.setRole(Role.APPLICANT);

        userRepository.save(user);
    }

    // LOGIN USER
    public AuthResponseDTO login(LoginRequestDTO request) {

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        // Verify password against stored BCrypt hash
        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if (!passwordMatches) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        // Generate JWT after successful authentication
        String token = jwtService.generateToken(user);

        // Return token to frontend / Postman
        return new AuthResponseDTO(token, "Bearer");
    }
}
