package com.supermarketpos.service;

import com.supermarketpos.dao.UserDao;
import com.supermarketpos.model.Role;
import com.supermarketpos.model.User;
import com.supermarketpos.util.HashUtil;
import com.supermarketpos.util.ValidationUtil;
import com.supermarketpos.util.ValidationUtil.ValidationResult;

import java.util.List;
import java.util.Optional;

public class UserService {

    private final UserDao userDao;

    public UserService() {
        this.userDao = new UserDao();
    }

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    public Optional<User> getUser(int id) {
        return userDao.findById(id);
    }

    public User createUser(String username, String plainPassword, Role role) {
        ValidationResult usernameCheck = ValidationUtil.validateUsername(username);
        if (!usernameCheck.isValid()) {
            throw new IllegalArgumentException(usernameCheck.getMessage());
        }
        ValidationResult passwordCheck = ValidationUtil.validatePassword(plainPassword);
        if (!passwordCheck.isValid()) {
            throw new IllegalArgumentException(passwordCheck.getMessage());
        }
        if (userDao.usernameExists(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(HashUtil.hash(plainPassword));
        user.setRole(role);
        user.setActive(true);
        return userDao.save(user);
    }

    public User updateUser(int id, String username, Role role) {
        User existing = userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        ValidationResult usernameCheck = ValidationUtil.validateUsername(username);
        if (!usernameCheck.isValid()) {
            throw new IllegalArgumentException(usernameCheck.getMessage());
        }
        if (!existing.getUsername().equals(username) && userDao.usernameExists(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        existing.setUsername(username);
        existing.setRole(role);
        return userDao.update(existing);
    }

    /** Soft delete — see UserDao.delete(). */
    public void deactivateUser(int id) {
        userDao.delete(id);
    }

    public void activateUser(int id) {
        User existing = userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        existing.setActive(true);
        userDao.update(existing);
    }

    public void resetPassword(int id, String newPassword) {
        User existing = userDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        ValidationResult passwordCheck = ValidationUtil.validatePassword(newPassword);
        if (!passwordCheck.isValid()) {
            throw new IllegalArgumentException(passwordCheck.getMessage());
        }
        existing.setPasswordHash(HashUtil.hash(newPassword));
        userDao.update(existing);
    }
}
