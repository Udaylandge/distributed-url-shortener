package com.uday.urlshortener.service;

import com.uday.urlshortener.model.User;

public interface UserService {

    // Existing
    User register(User user);
    boolean emailExists(String email);

    // Profile management
    User findByEmail(String email);
    User updateProfile(String email, String fullName);
    void changePassword(String email, String currentPassword, String newPassword);

    // Admin
    java.util.List<User> getAllUsers();
    void toggleUserActive(String userId);
    void deleteUser(String userId);
    long countAllUsers();
    long countActiveUsers();
}