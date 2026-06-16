package io.github.lucasfcz.olympusprotocol.exceptions;

import io.github.lucasfcz.olympusprotocol.dto.responses.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;

import java.util.UUID;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturn404WhenResourceNotFoundExceptionIsThrown() {
        // Arrange
        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        var exception = new ResourceNotFoundException("Exercise", id);

        var request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/exercises/" + id);

        // Act
        var response = handler.handleResourceNotFound(exception, request);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(exception.getMessage(), body.message());
    }

    @Test
    void shouldReturn409WhenDuplicateResourceExceptionIsThrown() {
        // Arrange
        var exception = new DuplicateResourceException("Resource already exists");

        // Act
        var response = handler.handleDuplicate(exception);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(exception.getMessage(), body.message());
    }

    @Test
    void shouldReturn403WhenForbiddenExceptionIsThrown() {
        // Arrange
        var exception = new ForbiddenException("Access denied");

        // Act
        var response = handler.handleForbidden(exception);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());

        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(exception.getMessage(), body.message());
    }

    @Test
    void shouldReturn400WhenBusinessExceptionIsThrown() {
        // Arrange
        var exception = new BusinessException("Business rule violation");

        // Act
        var response = handler.handleBusiness(exception);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertEquals(exception.getMessage(), body.message());
    }

    @Test
    void shouldReturn500WhenUnexpectedExceptionIsThrown() {
        // Arrange
        var exception = new RuntimeException("Unexpected error");

        var request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/test");

        // Act
        var response = handler.handleGeneric(exception, request);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        ErrorResponse body = response.getBody();
        assertNotNull(body);
        assertTrue(body.message().contains("Internal error"));
    }
}
