package com.sameneed.controller;

import com.sameneed.model.Notification;
import com.sameneed.service.NotificationService;
import com.sameneed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/notifications/*")
public class NotificationServlet extends HttpServlet {

    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            int userId = (int) req.getSession().getAttribute("userId");
            List<Notification> notifications = notificationService.getForUser(userId);
            res.getWriter().write(JsonUtil.toJson(notifications));
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Failed to load notifications\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            int userId = (int) req.getSession().getAttribute("userId");
            String path = req.getPathInfo();
            if ("/read-all".equals(path)) {
                notificationService.markAllRead(userId);
                res.getWriter().write("{\"message\":\"All notifications marked as read\"}");
            } else {
                res.setStatus(404);
                res.getWriter().write("{\"error\":\"Not found\"}");
            }
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Failed to update notifications\"}");
        }
    }
}
