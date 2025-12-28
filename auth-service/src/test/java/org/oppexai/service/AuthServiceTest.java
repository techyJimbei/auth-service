package org.oppexai.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAuthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import org.oppexai.model.User;
import org.oppexai.repository.UserRepository;
import org.oppexai.util.JwtUtil;
import org.oppexai.util.PasswordUtil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("AuthService Test Suite")
class AuthServiceTest {

    @Inject
    AuthService authService;

    @InjectMock
    UserService userService;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    JwtUtil jwtUtil;

    private User testUser;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Test@1234";
    private static final String TEST_TOKEN = "test-jwt-token";
    private static final Long TEST_USER_ID = 1L;

    @BeforeEach
    void setUp() {
        Mockito.reset(userService, userRepository, jwtUtil);

        testUser = new User(TEST_EMAIL, PasswordUtil.hashPassword(TEST_PASSWORD));
        testUser.setId(TEST_USER_ID);
        testUser.setIsVerified(true);
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLogin_Success() {
        // Arrange
        when(userService.findByEmail(TEST_EMAIL)).thenReturn(testUser);
        when(jwtUtil.generateToken(TEST_EMAIL, TEST_USER_ID, true)).thenReturn(TEST_TOKEN);

        // Act
        String token = authService.login(TEST_EMAIL, TEST_PASSWORD);

        // Assert
        assertNotNull(token);
        assertEquals(TEST_TOKEN, token);
        verify(userService, times(1)).findByEmail(TEST_EMAIL);
        verify(jwtUtil, times(1)).generateToken(TEST_EMAIL, TEST_USER_ID, true);
    }

    @Test
    @DisplayName("Should throw NotAuthorizedException with incorrect password")
    void testLogin_IncorrectPassword() {

        String wrongPassword = "WrongPassword@123";
        when(userService.findByEmail(TEST_EMAIL)).thenReturn(testUser);

        NotAuthorizedException exception = assertThrows(
                NotAuthorizedException.class,
                () -> authService.login(TEST_EMAIL, wrongPassword)
        );

        assertNotNull(exception);
        verify(userService, times(1)).findByEmail(TEST_EMAIL);
        verify(jwtUtil, never()).generateToken(anyString(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Should throw NotAuthorizedException when user not found")
    void testLogin_UserNotFound() {
        when(userService.findByEmail(TEST_EMAIL))
                .thenThrow(new jakarta.ws.rs.NotFoundException("User not found"));

        assertThrows(
                jakarta.ws.rs.NotFoundException.class,
                () -> authService.login(TEST_EMAIL, TEST_PASSWORD)
        );

        verify(userService, times(1)).findByEmail(TEST_EMAIL);
        verify(jwtUtil, never()).generateToken(anyString(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Should successfully login unverified user")
    void testLogin_UnverifiedUser() {
        testUser.setIsVerified(false);
        when(userService.findByEmail(TEST_EMAIL)).thenReturn(testUser);
        when(jwtUtil.generateToken(TEST_EMAIL, TEST_USER_ID, false)).thenReturn(TEST_TOKEN);

        String token = authService.login(TEST_EMAIL, TEST_PASSWORD);

        assertNotNull(token);
        assertEquals(TEST_TOKEN, token);
        verify(userService, times(1)).findByEmail(TEST_EMAIL);
        verify(jwtUtil, times(1)).generateToken(TEST_EMAIL, TEST_USER_ID, false);
    }

    @Test
    @DisplayName("Should handle null password gracefully")
    void testLogin_NullPassword() {
        when(userService.findByEmail(TEST_EMAIL)).thenReturn(testUser);

        assertThrows(
                Exception.class,
                () -> authService.login(TEST_EMAIL, null)
        );

        verify(userService, times(1)).findByEmail(TEST_EMAIL);
        verify(jwtUtil, never()).generateToken(anyString(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Should handle empty password")
    void testLogin_EmptyPassword() {
        when(userService.findByEmail(TEST_EMAIL)).thenReturn(testUser);

        NotAuthorizedException exception = assertThrows(
                NotAuthorizedException.class,
                () -> authService.login(TEST_EMAIL, "")
        );

        assertNotNull(exception);
        verify(userService, times(1)).findByEmail(TEST_EMAIL);
        verify(jwtUtil, never()).generateToken(anyString(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Should successfully get user from token")
    void testGetUserFromToken_Success() {

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        User result = authService.getUserFromToken(TEST_EMAIL);

        assertNotNull(result);
        assertEquals(TEST_EMAIL, result.getEmail());
        assertEquals(TEST_USER_ID, result.getId());
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should throw NotAuthorizedException when user not found by token")
    void testGetUserFromToken_UserNotFound() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        NotAuthorizedException exception = assertThrows(
                NotAuthorizedException.class,
                () -> authService.getUserFromToken(TEST_EMAIL)
        );
        assertNotNull(exception);
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }
}