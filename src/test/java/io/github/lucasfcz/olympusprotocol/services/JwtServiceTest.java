package io.github.lucasfcz.olympusprotocol.services;

import com.auth0.jwt.JWT;
import io.github.lucasfcz.olympusprotocol.models.User;
import io.github.lucasfcz.olympusprotocol.models.enums.ExperienceLevel;
import io.github.lucasfcz.olympusprotocol.models.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Tests")
class JwtServiceTest {

    private static final String TEST_SECRET = "test-secret-key-for-jwt-testing";
    private static final String TEST_EMAIL = "test@example.com";

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();
        setPrivateField("secret", TEST_SECRET);
        setPrivateField("expiration", 3600_000L);
    }

    private void setPrivateField(String fieldName, Object value) throws Exception {
        Field field = JwtService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(jwtService, value);
    }

    @Test
    @DisplayName("generateToken should create non-null token containing email subject")
    void generateToken_validUser_shouldCreateTokenWithEmailSubject() {
        // Arrange
        var user = new User("Tony Stark", "test@example.com", "testpassword", ExperienceLevel.BEGINNER, 102.3, 1.90);

        // Act
        var token = jwtService.generateToken(user);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());

        var decoded = JWT.decode(token);
        assertEquals(TEST_EMAIL, decoded.getSubject());
    }

    @Test
    @DisplayName("generateToken should include role claim in token")
    void generateToken_validUser_shouldIncludeRoleClaim() {
        // Arrange
        var user = new User("Patrick Jane",TEST_EMAIL,"passwordUltraSecret" , ExperienceLevel.INTERMEDIATE, 70.4, 1.74);

        // Act
        var token = jwtService.generateToken(user);

        // Assert
        var decoded = JWT.decode(token);
        assertNotNull(decoded.getClaim("role"));
        assertEquals(Role.USER.name(), decoded.getClaim("role").asString());
    }

    @Test
    @DisplayName("extractEmail should extract correct email from valid token")
    void extractEmail_validToken_shouldReturnCorrectEmail() {
        // Arrange
        var user = new User("Tony Stark", "test@example.com", "testpassword", ExperienceLevel.ADVANCED, 102.3, 1.90);
        var token = jwtService.generateToken(user);

        // Act
        var extractedEmail = jwtService.extractEmail(token);

        // Assert
        assertEquals(TEST_EMAIL, extractedEmail);
    }

    @Test
    @DisplayName("isTokenValid should return true for valid token")
    void isTokenValid_validToken_shouldReturnTrue() {
        // Arrange
        var user = new User("Tony Stark", "test@example.com", "testpassword", ExperienceLevel.EXPERT, 102.3, 1.90);
        var token = jwtService.generateToken(user);

        // Act
        var isValid = jwtService.isTokenValid(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("isTokenValid should return false for expired token")
    void isTokenValid_expiredToken_shouldReturnFalse() throws Exception {
        // Arrange
        setPrivateField("expiration", -1000L); // Negative expiration makes token expire immediately
        var user = new User("Tony Stark", "test@example.com", "testpassword", ExperienceLevel.BEGINNER, 102.3, 1.90);
        var expiredToken = jwtService.generateToken(user);

        // Act
        var isValid = jwtService.isTokenValid(expiredToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("isTokenValid should return false for tampered token")
    void isTokenValid_tamperedToken_shouldReturnFalse() {
        // Arrange
        var user = new User("Tony Stark", "test@example.com", "testpassword", ExperienceLevel.INTERMEDIATE, 102.3, 1.90);
        var validToken = jwtService.generateToken(user);
        var tamperedToken = validToken + "TAMPERED";

        // Act
        var isValid = jwtService.isTokenValid(tamperedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("isTokenValid should return false for completely invalid token")
    void isTokenValid_invalidToken_shouldReturnFalse() {
        // Arrange
        var invalidToken = "invalid.jwt.token";

        // Act
        var isValid = jwtService.isTokenValid(invalidToken);

        // Assert
        assertFalse(isValid);
    }
}

