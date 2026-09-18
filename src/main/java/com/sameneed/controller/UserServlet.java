package com.sameneed.controller;

import com.sameneed.dao.UserDao;
import com.sameneed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/users/*")
public class UserServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            int userId = (int) req.getSession().getAttribute("userId");
            res.getWriter().write(JsonUtil.toJson(userDao.findById(userId)));
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Internal server error\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            int sessionUserId = (int) req.getSession().getAttribute("userId");
            String path = req.getPathInfo();
            int targetId = Integer.parseInt(path.replaceAll("[^0-9]", "").trim());

            if (sessionUserId != targetId) {
                res.setStatus(403);
                res.getWriter().write("{\"error\":\"You can only update your own profile\"}");
                return;
            }

            Map<String, Object> body = JsonUtil.fromJson(req.getReader(), Map.class);
            if (body == null) {
                res.setStatus(400);
                res.getWriter().write("{\"error\":\"Invalid body\"}");
                return;
            }

            String displayName = (String) body.get("displayName");
            String phone = (String) body.get("phone");
            String locality = (String) body.get("locality");

            userDao.updateProfile(targetId, displayName, phone, locality);

            String role = (String) req.getSession().getAttribute("role");
            if ("SERVICE_PROVIDER".equals(role)) {
                String businessName = (String) body.get("businessName");
                String bio = (String) body.get("bio");
                String serviceArea = (String) body.get("serviceArea");
                if (businessName != null) {
                    userDao.updateProviderProfile(targetId, businessName, bio, serviceArea);
                }
            }

            res.getWriter().write("{\"message\":\"Profile updated\"}");
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Internal server error\"}");
        }
    }
}
