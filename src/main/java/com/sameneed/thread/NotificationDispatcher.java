package com.sameneed.thread;

import com.sameneed.dao.NotificationDao;
import com.sameneed.model.Notification;

import java.util.concurrent.LinkedBlockingQueue;

public class NotificationDispatcher implements Runnable {

    private static NotificationDispatcher instance;
    private final LinkedBlockingQueue<Notification> queue = new LinkedBlockingQueue<>();
    private final NotificationDao notificationDao = new NotificationDao();
    private volatile boolean running = false;
    private Thread workerThread;

    private NotificationDispatcher() {}

    public static synchronized NotificationDispatcher getInstance() {
        if (instance == null) {
            instance = new NotificationDispatcher();
        }
        return instance;
    }

    public void start() {
        running = true;
        workerThread = new Thread(this, "NotificationDispatcher-Thread");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    public void stop() {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    public void enqueue(Notification notification) {
        queue.offer(notification);
    }

    @Override
    public void run() {
        while (running) {
            try {
                Notification notification = queue.take();
                try {
                    notificationDao.insert(notification);
                } catch (Exception e) {
                    queue.offer(notification);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        Notification remaining;
        while ((remaining = queue.poll()) != null) {
            try {
                notificationDao.insert(remaining);
            } catch (Exception ignored) {}
        }
    }
}
