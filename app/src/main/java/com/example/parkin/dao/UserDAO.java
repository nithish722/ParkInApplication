package com.example.parkin.dao;

import com.example.parkin.database.MockDatabaseStore;
import com.example.parkin.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User findByUsernameOrEmail(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) return null;
        String cleanId = identifier.trim();

        for (User u : MockDatabaseStore.getInstance().getUsers().values()) {
            if ((cleanId.equalsIgnoreCase(u.getUsername()) || cleanId.equalsIgnoreCase(u.getEmail()))
                    && !"SUSPENDED".equalsIgnoreCase(u.getStatus())) {
                return u;
            }
        }
        return null;
    }

    public User findById(int userId) {
        return MockDatabaseStore.getInstance().getUsers().get(userId);
    }

    public List<User> findAll() {
        return new ArrayList<>(MockDatabaseStore.getInstance().getUsers().values());
    }

    public boolean insert(User user) {
        if (user == null) return false;
        int newId = MockDatabaseStore.getInstance().nextUserId();
        user.setUserId(newId);
        MockDatabaseStore.getInstance().getUsers().put(newId, user);
        return true;
    }

    public boolean update(User user) {
        if (user == null || !MockDatabaseStore.getInstance().getUsers().containsKey(user.getUserId())) {
            return false;
        }
        MockDatabaseStore.getInstance().getUsers().put(user.getUserId(), user);
        return true;
    }

    public boolean updatePassword(int userId, String newPassword) {
        User u = MockDatabaseStore.getInstance().getUsers().get(userId);
        if (u != null) {
            u.setPassword(newPassword);
            return true;
        }
        return false;
    }

    public boolean isEmailOrUsernameTaken(String email, String username, int excludeUserId) {
        for (User u : MockDatabaseStore.getInstance().getUsers().values()) {
            if (u.getUserId() != excludeUserId) {
                if ((email != null && email.equalsIgnoreCase(u.getEmail())) ||
                        (username != null && username.equalsIgnoreCase(u.getUsername()))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean delete(int userId) {
        return MockDatabaseStore.getInstance().getUsers().remove(userId) != null;
    }
}