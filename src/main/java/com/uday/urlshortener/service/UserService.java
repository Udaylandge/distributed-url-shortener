package com.uday.urlshortener.service;

import com.uday.urlshortener.model.User;

import java.util.List;

public interface UserService {

    User register(User user);

    boolean emailExists(String email);

    User findByEmail(String email);

    User updateProfile(String email, String fullName);

    void changePassword(String email, String currentPassword, String newPassword);

    List<User> getAllUsers();

    void toggleUserActive(String userId);

    void deleteUser(String userId);

    long countAllUsers();

    long countActiveUsers();

    boolean verifyEmail(String token);

    void createPasswordResetToken(String email);

    boolean validatePasswordResetToken(String token);

    void resetPassword(String token, String newPassword);
}