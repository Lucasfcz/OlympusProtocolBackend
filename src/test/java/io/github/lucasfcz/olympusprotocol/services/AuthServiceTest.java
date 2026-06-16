package io.github.lucasfcz.olympusprotocol.services;

import io.github.lucasfcz.olympusprotocol.dto.requests.LoginRequest;
import io.github.lucasfcz.olympusprotocol.dto.requests.RegisterRequest;
import io.github.lucasfcz.olympusprotocol.exceptions.DuplicateResourceException;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.models.enums.ExperienceLevel;
import io.github.lucasfcz.olympusprotocol.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "securePassword123";

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("register should successfully create new user and return auth response")
    void register_validRequest_shouldCreateUserAndReturnToken() {
        // Arrange
        var request = new RegisterRequest("Patrick Jane",TEST_EMAIL, TEST_PASSWORD, ExperienceLevel.INTERMEDIATE, 70.4, 1.74);
        var encodedPassword = "$2a$10$encodedhash";
        var token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";

        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(encodedPassword);
        when(jwtService.generateToken(any(User.class))).thenReturn(token);

        // Act
        var response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(token, response.token());
        assertEquals(TEST_EMAIL, response.email());
        assertEquals("Patrick Jane", response.name());
        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    @DisplayName("register should throw DuplicateResourceException when email already exists")
    void register_duplicateEmail_shouldThrowDuplicateResourceException() {
        // Arrange
        var request = new RegisterRequest("Patrick Jane",TEST_EMAIL, TEST_PASSWORD, ExperienceLevel.INTERMEDIATE, 70.4, 1.74);
        lenient().when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("login should authenticate user and return auth response with token")
    void login_validCredentials_shouldAuthenticateAndReturnToken() {
        // Arrange
        var request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
        var user = new User("Patrick Jane",TEST_EMAIL, TEST_PASSWORD, ExperienceLevel.INTERMEDIATE, 70.4, 1.74);
        var token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(token);

        // Act
        var response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(token, response.token());
        assertEquals(TEST_EMAIL, response.email());
        assertEquals("Patrick Jane", response.name());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail(TEST_EMAIL);
        verify(jwtService).generateToken(user);
    }

    @Test
    @DisplayName("login should throw exception when credentials are invalid")
    void login_invalidCredentials_shouldThrowBadCredentialsException() {
        // Arrange
        var request = new LoginRequest(TEST_EMAIL, "wrongPassword");
        doThrow(new BadCredentialsException("Invalid credentials"))
            .when(authenticationManager)
            .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(any());
    }
}

