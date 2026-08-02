package com.uday.urlshortener.service;

import com.uday.urlshortener.model.User;
import com.uday.urlshortener.repository.UserRepository;
import com.uday.urlshortener.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, passwordEncoder, emailService);
    }

    @Test
    @DisplayName("register saves new user with encoded password and USER role")
    void register_newUser_savesWithEncodedPasswordAndUserRole() {
        User input = buildUser("New User", "new@test.com", "plainPass");
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPass")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.register(input);

        assertThat(result.getPassword()).isEqualTo("encodedPass");
        assertThat(result.getRole()).isEqualTo("USER");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getCreatedAt()).isNotNull();
        verify(userRepository).save(input);
    }

    @Test
    @DisplayName("register throws when email already exists")
    void register_duplicateEmail_throwsException() {
        User input = buildUser("Dup User", "dup@test.com", "pass");
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(input))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("emailExists returns true when email is registered")
    void emailExists_registeredEmail_returnsTrue() {
        when(userRepository.existsByEmail("exists@test.com")).thenReturn(true);
        assertThat(userService.emailExists("exists@test.com")).isTrue();
    }

    @Test
    @DisplayName("emailExists returns false when email is not registered")
    void emailExists_unregisteredEmail_returnsFalse() {
        when(userRepository.existsByEmail("nope@test.com")).thenReturn(false);
        assertThat(userService.emailExists("nope@test.com")).isFalse();
    }

    @Test
    @DisplayName("findByEmail returns user when found")
    void findByEmail_existingUser_returnsUser() {
        User user = buildUser("Found", "found@test.com", "pass");
        when(userRepository.findByEmail("found@test.com")).thenReturn(Optional.of(user));

        User result = userService.findByEmail("found@test.com");

        assertThat(result.getEmail()).isEqualTo("found@test.com");
    }

    @Test
    @DisplayName("findByEmail throws when user not found")
    void findByEmail_notFound_throwsException() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("missing@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("updateProfile changes fullName and saves")
    void updateProfile_validUser_updatesName() {
        User user = buildUser("Old Name", "user@test.com", "pass");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateProfile("user@test.com", "New Name");

        assertThat(result.getFullName()).isEqualTo("New Name");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("changePassword succeeds when current password matches")
    void changePassword_correctCurrentPassword_updatesPassword() {
        User user = buildUser("User", "user@test.com", "encodedOld");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "encodedOld")).thenReturn(true);
        when(passwordEncoder.encode("newPass8Char")).thenReturn("encodedNew");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.changePassword("user@test.com", "oldPass", "newPass8Char");

        assertThat(user.getPassword()).isEqualTo("encodedNew");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("changePassword throws when current password is wrong")
    void changePassword_incorrectCurrentPassword_throwsException() {
        User user = buildUser("User", "user@test.com", "encodedOld");
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "encodedOld")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("user@test.com", "wrongPass", "newPass8Char"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("incorrect");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("toggleUserActive flips active status from true to false")
    void toggleUserActive_activeUser_deactivates() {
        User user = buildUser("User", "user@test.com", "pass");
        user.setId("user1");
        user.setActive(true);
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.toggleUserActive("user1");

        assertThat(user.isActive()).isFalse();
    }

    @Test
    @DisplayName("toggleUserActive flips active status from false to true")
    void toggleUserActive_inactiveUser_activates() {
        User user = buildUser("User", "user@test.com", "pass");
        user.setId("user1");
        user.setActive(false);
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.toggleUserActive("user1");

        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("countActiveUsers returns count of active users only")
    void countActiveUsers_mixedUsers_returnsActiveCount() {
        User active1 = buildUser("A1", "a1@test.com", "p");
        active1.setActive(true);
        User active2 = buildUser("A2", "a2@test.com", "p");
        active2.setActive(true);
        User inactive = buildUser("I1", "i1@test.com", "p");
        inactive.setActive(false);

        when(userRepository.findAll()).thenReturn(List.of(active1, active2, inactive));

        assertThat(userService.countActiveUsers()).isEqualTo(2L);
    }

    private User buildUser(String fullName, String email, String password) {
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole("USER");
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return user;
    }
}
