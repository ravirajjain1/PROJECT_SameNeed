package com.sameneed.service;

import com.sameneed.dao.NotificationDao;
import com.sameneed.model.Notification;
import com.sameneed.thread.NotificationDispatcher;

import java.util.List;

public class NotificationService {

    private final NotificationDao notificationDao = new NotificationDao();

    public void queueForUser(int userId, String message) {
        Notification notification = new Notification(userId, message);
        NotificationDispatcher.getInstance().enqueue(notification);
    }

    public List<Notification> getForUser(int userId) {
        return notificationDao.findByUser(userId);
    }

    public int countUnread(int userId) {
        return notificationDao.countUnread(userId);
    }

    public void markAllRead(int userId) {
        notificationDao.markAllRead(userId);
    }
}
