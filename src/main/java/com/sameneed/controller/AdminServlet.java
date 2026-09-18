package com.sameneed.controller;

import com.sameneed.dao.RequestDao;
import com.sameneed.dao.UserDao;
import com.sameneed.dao.jpa.ProviderJpaDao;
import com.sameneed.enums.UserRole;
import com.sameneed.model.BaseUser;
import com.sameneed.service.BookingService;
import com.sameneed.service.ReportService;
import com.sameneed.service.RequestService;
import com.sameneed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {

    private final UserDao userDao = new UserDao();
    private final RequestService requestService = new RequestService();
    private final BookingService bookingService = new BookingService();
    private final ReportService reportService = new ReportService();
    private final ProviderJpaDao providerJpaDao = new ProviderJpaDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        String path = req.getPathInfo();

        try {
            if ("/dashboard".equals(path)) {
                handleDashboard(req, res);
            } else if ("/users".equals(path)) {
                List<BaseUser> customers = userDao.findAllByRole(UserRole.CUSTOMER);
                res.getWriter().write(JsonUtil.toJson(customers));
            } else if ("/providers".equals(path)) {
                List<BaseUser> providers = userDao.findAllByRole(UserRole.SERVICE_PROVIDER);
                res.getWriter().write(JsonUtil.toJson(providers));
            } else if ("/requests".equals(path)) {
                int limit = parseIntParam(req, "limit", 50);
                int offset = parseIntParam(req, "offset", 0);
                res.getWriter().write(JsonUtil.toJson(requestService.getAll(limit, offset)));
            } else if ("/bookings".equals(path)) {
                int limit = parseIntParam(req, "limit", 50);
                int offset = parseIntParam(req, "offset", 0);
                res.getWriter().write(JsonUtil.toJson(bookingService.getAll(limit, offset)));
            } else if ("/reports".equals(path)) {
                res.getWriter().write(JsonUtil.toJson(reportService.getAllReports()));
            } else {
                res.setStatus(404);
                res.getWriter().write("{\"error\":\"Not found\"}");
            }
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Internal server error\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        String path = req.getPathInfo();

        try {
            Map<String, Object> body = JsonUtil.fromJson(req.getReader(), Map.class);

            if (path != null && path.startsWith("/users/") && path.endsWith("/toggle-active")) {
                int targetUserId = extractId(path, "/users/", "/toggle-active");
                boolean currentStatus = userDao.findById(targetUserId).isActive();
                userDao.setActiveStatus(targetUserId, !currentStatus);
                res.getWriter().write("{\"message\":\"User status updated\"}");

            } else if (path != null && path.startsWith("/providers/") && path.endsWith("/verify")) {
                int providerId = extractId(path, "/providers/", "/verify");
                userDao.setProviderVerified(providerId, true);
                res.getWriter().write("{\"message\":\"Provider verified\"}");

            } else if (path != null && path.startsWith("/reports/") && path.endsWith("/resolve")) {
                int reportId = extractId(path, "/reports/", "/resolve");
                String resolution = (String) body.get("resolution");
                reportService.resolveReport(reportId, resolution);
                res.getWriter().write("{\"message\":\"Report updated\"}");

            } else {
                res.setStatus(404);
                res.getWriter().write("{\"error\":\"Not found\"}");
            }
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Internal server error\"}");
        }
    }

    private void handleDashboard(HttpServletRequest req, HttpServletResponse res) throws IOException {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRequests", requestService.countAll());
        stats.put("totalBookings", bookingService.countAll());
        stats.put("totalCustomers", userDao.findAllByRole(UserRole.CUSTOMER).size());
        stats.put("totalProviders", userDao.findAllByRole(UserRole.SERVICE_PROVIDER).size());
        stats.put("openReports", reportService.countOpenReports());
        stats.put("verifiedProviders", providerJpaDao.countVerified());
        res.getWriter().write(JsonUtil.toJson(stats));
    }

    private int extractId(String path, String prefix, String suffix) {
        return Integer.parseInt(path.replace(prefix, "").replace(suffix, "").trim());
    }

    private int parseIntParam(HttpServletRequest req, String name, int defaultVal) {
        String val = req.getParameter(name);
        if (val == null) return defaultVal;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return defaultVal; }
    }
}
