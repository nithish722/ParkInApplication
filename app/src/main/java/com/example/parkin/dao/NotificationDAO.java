package com.example.parkin.dao;

import com.example.parkin.database.MockDatabaseStore;
import com.example.parkin.models.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public List<Notification> findAll() {
        List<Notification> list = new ArrayList<>(MockDatabaseStore.getInstance().getNotifications().values());
        list.sort((a, b) -> {
            if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });
        return list;
    }

    public List<Notification> findForRole(String role) {
        List<Notification> list = new ArrayList<>();
        for (Notification n : MockDatabaseStore.getInstance().getNotifications().values()) {
            if ("ALL".equalsIgnoreCase(n.getTargetRole()) ||
                    (role != null && role.equalsIgnoreCase(n.getTargetRole()))) {
                list.add(n);
            }
        }
        list.sort((a, b) -> {
            if (a.getCreatedAt() == null || b.getCreatedAt() == null) return 0;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });
        return list;
    }

    public Notification findById(int notificationId) {
        return MockDatabaseStore.getInstance().getNotifications().get(notificationId);
    }

    public boolean insert(Notification notification) {
        if (notification == null) return false;
        int newId = MockDatabaseStore.getInstance().nextNotificationId();
        notification.setNotificationId(newId);
        MockDatabaseStore.getInstance().getNotifications().put(newId, notification);
        return true;
    }

    public boolean delete(int notificationId) {
        return MockDatabaseStore.getInstance().getNotifications().remove(notificationId) != null;
    }
}