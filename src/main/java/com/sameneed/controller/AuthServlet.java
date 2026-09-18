package com.sameneed.controller;

import com.sameneed.exception.AuthException;
import com.sameneed.model.BaseUser;
import com.sameneed.service.AuthService;
import com.sameneed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        String path = req.getPathInfo();

        try {
            if ("/login".equals(path)) {
                handleLogin(req, res);
            } else if ("/register".equals(path)) {
                handleRegister(req, res);
            } else if ("/logout".equals(path)) {
                handleLogout(req, res);
            } else {
                res.setStatus(404);
                res.getWriter().write("{\"error\":\"Not found\"}");
            }
        } catch (AuthException e) {
            res.setStatus(e.getHttpStatus());
            res.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Internal server error\"}");
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Map<String, Object> body = JsonUtil.fromJson(req.getReader(), Map.class);
        if (body == null) throw new AuthException("Invalid request body", 400);

        String email = (String) body.get("email");
        String password = (String) body.get("password");

        BaseUser user = authService.login(email, password);

        HttpSession session = req.getSession(true);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("role", user.getRole().name());
        session.setAttribute("displayName", user.getDisplayName());
        session.setMaxInactiveInterval(3600);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getUserId());
        response.put("role", user.getRole().name());
        response.put("displayName", user.getDisplayName());
        res.getWriter().write(JsonUtil.toJson(response));
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Map<String, Object> body = JsonUtil.fromJson(req.getReader(), Map.class);
        if (body == null) throw new AuthException("Invalid request body", 400);

        String role = (String) body.get("role");
        BaseUser user;

        if ("CUSTOMER".equalsIgnoreCase(role)) {
            user = authService.registerCustomer(
                    (String) body.get("email"),
                    (String) body.get("password"),
                    (String) body.get("displayName"),
                    (String) body.get("phone"),
                    (String) body.get("locality"));
        } else if ("SERVICE_PROVIDER".equalsIgnoreCase(role)) {
            user = authService.registerProvider(
                    (String) body.get("email"),
                    (String) body.get("password"),
                    (String) body.get("displayName"),
                    (String) body.get("phone"),
                    (String) body.get("businessName"),
                    (String) body.get("bio"),
                    (String) body.get("serviceArea"));
        } else {
            throw new AuthException("Invalid role. Choose CUSTOMER or SERVICE_PROVIDER", 400);
        }

        res.setStatus(201);
        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getUserId());
        response.put("role", user.getRole().name());
        response.put("message", "Registration successful");
        res.getWriter().write(JsonUtil.toJson(response));
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
        res.getWriter().write("{\"message\":\"Logged out\"}");
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "'");
    }
}
