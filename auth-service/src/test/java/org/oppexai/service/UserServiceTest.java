package org.oppexai.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import org.oppexai.model.User;
import org.oppexai.repository.UserRepository;
import org.oppexai.util.PasswordUtil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("UserService Test Suite")
class UserServiceTest {

    @Inject
    UserService userService;

    @InjectMock
    UserRepository userRepository;

    @InjectMock
    EmailService emailService;

    private User testUser;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Test@1234";
    private static final String TEST_TOKEN = "test-verification-token";

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        Mockito.reset(userRepository, emailService);

        // Create test user
        testUser = new User(TEST_EMAIL, PasswordUtil.hashPassword(TEST_PASSWORD));
        testUser.setId(1L);
        testUser.setIsVerified(false);
        testUser.setVerificationToken(TEST_TOKEN);
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void testSignup_Success() {
        // Arrange
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        doNothing().when(userRepository).persist(any(User.class));
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        // Act
        assertDoesNotThrow(() -> userService.signup(TEST_EMAIL, TEST_PASSWORD));

        // Assert
        verify(userRepository, times(1)).existsByEmail(TEST_EMAIL);
        verify(userRepository, times(1)).persist(any(User.class));
        verify(emailService, times(1)).sendVerificationEmail(eq(TEST_EMAIL), anyString());
    }

    @Test
    @DisplayName("Should throw BadRequestException when email already exists")
    void testSignup_EmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> userService.signup(TEST_EMAIL, TEST_PASSWORD)
        );

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, times(1)).existsByEmail(TEST_EMAIL);
        verify(userRepository, never()).persist(any(User.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Should continue signup even if email sending fails")
    void testSignup_EmailSendingFails() {
        // Arrange
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        doNothing().when(userRepository).persist(any(User.class));
        doThrow(new RuntimeException("Email service down"))
                .when(emailService).sendVerificationEmail(anyString(), anyString());

        // Act & Assert - Should not throw exception
        assertDoesNotThrow(() -> userService.signup(TEST_EMAIL, TEST_PASSWORD));

        verify(userRepository, times(1)).persist(any(User.class));
    }

    @Test
    @DisplayName("Should successfully verify email with valid token")
    void testVerifyEmail_Success() {
        // Arrange
        when(userRepository.findByVerificationToken(TEST_TOKEN))
                .thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).persist(any(User.class));

        // Act
        assertDoesNotThrow(() -> userService.verifyEmail(TEST_TOKEN));

        // Assert
        assertTrue(testUser.getIsVerified());
        assertNull(testUser.getVerificationToken());
        verify(userRepository, times(1)).findByVerificationToken(TEST_TOKEN);
        verify(userRepository, times(1)).persist(testUser);
    }

    @Test
    @DisplayName("Should throw NotFoundException with invalid verification token")
    void testVerifyEmail_InvalidToken() {
        // Arrange
        when(userRepository.findByVerificationToken(TEST_TOKEN))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.verifyEmail(TEST_TOKEN)
        );

        assertEquals("Invalid or expired verification token", exception.getMessage());
        verify(userRepository, times(1)).findByVerificationToken(TEST_TOKEN);
        verify(userRepository, never()).persist(any(User.class));
    }

    @Test
    @DisplayName("Should handle already verified email gracefully")
    void testVerifyEmail_AlreadyVerified() {
        // Arrange
        testUser.setIsVerified(true);
        when(userRepository.findByVerificationToken(TEST_TOKEN))
                .thenReturn(Optional.of(testUser));

        // Act
        assertDoesNotThrow(() -> userService.verifyEmail(TEST_TOKEN));

        // Assert
        assertTrue(testUser.getIsVerified());
        verify(userRepository, times(1)).findByVerificationToken(TEST_TOKEN);
        // persist should not be called for already verified user
    }

    @Test
    @DisplayName("Should find user by email successfully")
    void testFindByEmail_Success() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));

        // Act
        User foundUser = userService.findByEmail(TEST_EMAIL);

        // Assert
        assertNotNull(foundUser);
        assertEquals(TEST_EMAIL, foundUser.getEmail());
        assertEquals(testUser.getId(), foundUser.getId());
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should throw NotFoundException when user not found by email")
    void testFindByEmail_NotFound() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.findByEmail(TEST_EMAIL)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should find user by ID successfully")
    void testFindById_Success() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findByIdOptional(userId))
                .thenReturn(Optional.of(testUser));

        // Act
        User foundUser = userService.findById(userId);

        // Assert
        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
        verify(userRepository, times(1)).findByIdOptional(userId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when user not found by ID")
    void testFindById_NotFound() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findByIdOptional(userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.findById(userId)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByIdOptional(userId);
    }

    @Test
    @DisplayName("Should return true when email exists")
    void testEmailExists_True() {
        // Arrange
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        // Act
        boolean exists = userService.emailExists(TEST_EMAIL);

        // Assert
        assertTrue(exists);
        verify(userRepository, times(1)).existsByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void testEmailExists_False() {
        // Arrange
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);

        // Act
        boolean exists = userService.emailExists(TEST_EMAIL);

        // Assert
        assertFalse(exists);
        verify(userRepository, times(1)).existsByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should return correct verification status")
    void testIsUserVerified() {
        // Arrange
        testUser.setIsVerified(true);
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));

        // Act
        boolean isVerified = userService.isUserVerified(TEST_EMAIL);

        // Assert
        assertTrue(isVerified);
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should successfully resend verification email")
    void testResendVerificationEmail_Success() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).persist(any(User.class));
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        // Act
        assertDoesNotThrow(() -> userService.resendVerificationEmail(TEST_EMAIL));

        // Assert
        assertNotNull(testUser.getVerificationToken());
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
        verify(userRepository, times(1)).persist(testUser);
        verify(emailService, times(1)).sendVerificationEmail(eq(TEST_EMAIL), anyString());
    }

    @Test
    @DisplayName("Should throw BadRequestException when resending to verified user")
    void testResendVerificationEmail_AlreadyVerified() {
        // Arrange
        testUser.setIsVerified(true);
        when(userRepository.findByEmail(TEST_EMAIL))
                .thenReturn(Optional.of(testUser));

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> userService.resendVerificationEmail(TEST_EMAIL)
        );

        assertEquals("Email already verified", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
        verify(userRepository, never()).persist(any(User.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

}