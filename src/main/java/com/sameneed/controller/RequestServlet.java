package com.sameneed.controller;

import com.sameneed.enums.RequestStatus;
import com.sameneed.exception.InvalidRequestException;
import com.sameneed.model.ServiceRequest;
import com.sameneed.service.MatchingService;
import com.sameneed.service.RequestService;
import com.sameneed.util.DateUtil;
import com.sameneed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/requests/*")
public class RequestServlet extends HttpServlet {

    private final RequestService requestService = new RequestService();
    private final MatchingService matchingService = new MatchingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        String path = req.getPathInfo();

        try {
            if (path == null || path.equals("/")) {
                handleListRequests(req, res);
            } else if (path.equals("/match")) {
                handleMatch(req, res);
            } else if (path.equals("/my")) {
                handleMyRequests(req, res);
            } else {
                int requestId = parseId(path);
                ServiceRequest sr = requestService.getById(requestId);
                res.getWriter().write(JsonUtil.toJson(sr));
            }
        } catch (InvalidRequestException e) {
            res.setStatus(400);
            res.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Internal server error\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            int userId = (int) req.getSession().getAttribute("userId");
            String role = (String) req.getSession().getAttribute("role");
            if (!"CUSTOMER".equals(role)) {
                res.setStatus(403);
                res.getWriter().write("{\"error\":\"Only customers can create service requests\"}");
                return;
            }

            Map<String, Object> body = JsonUtil.fromJson(req.getReader(), Map.class);
            if (body == null) throw new InvalidRequestException("Invalid request body");

            int serviceId = ((Number) body.get("serviceId")).intValue();
            String problem = (String) body.get("problemDescription");
            String locality = (String) body.get("locality");
            String dateStr = (String) body.get("preferredDate");
            String timeStr = (String) body.get("preferredTime");
            String budgetStr = body.get("budgetExpectation") != null ? body.get("budgetExpectation").toString() : null;
            int maxGroupSize = body.get("maxGroupSize") != null ? ((Number) body.get("maxGroupSize")).intValue() : 10;

            ServiceRequest created = requestService.createRequest(userId, serviceId, problem, locality,
                    dateStr, timeStr, budgetStr, maxGroupSize);

            res.setStatus(201);
            res.getWriter().write(JsonUtil.toJson(created));

        } catch (InvalidRequestException e) {
            res.setStatus(400);
            res.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Internal server error\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            int userId = (int) req.getSession().getAttribute("userId");
            String path = req.getPathInfo();
            int requestId = parseId(path);

            Map<String, Object> body = JsonUtil.fromJson(req.getReader(), Map.class);
            if (body == null) throw new InvalidRequestException("Invalid request body");

            String statusStr = (String) body.get("status");
            RequestStatus newStatus = RequestStatus.valueOf(statusStr.toUpperCase());
            requestService.updateStatus(requestId, newStatus, userId);

            res.getWriter().write("{\"message\":\"Status updated\"}");

        } catch (IllegalArgumentException e) {
            res.setStatus(400);
            res.getWriter().write("{\"error\":\"Invalid status value\"}");
        } catch (InvalidRequestException e) {
            res.setStatus(400);
            res.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Internal server error\"}");
        }
    }

    private void handleListRequests(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String role = (String) req.getSession().getAttribute("role");
        int userId = (int) req.getSession().getAttribute("userId");

        if ("SERVICE_PROVIDER".equals(role)) {
            List<ServiceRequest> list = requestService.getEligibleForProvider(userId);
            res.getWriter().write(JsonUtil.toJson(list));
        } else {
            List<ServiceRequest> list = requestService.getOpenRequests();
            res.getWriter().write(JsonUtil.toJson(list));
        }
    }

    private void handleMatch(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String serviceIdStr = req.getParameter("serviceId");
        String locality = req.getParameter("locality");
        String dateStr = req.getParameter("preferredDate");

        if (serviceIdStr == null || locality == null || dateStr == null) {
            res.setStatus(400);
            res.getWriter().write("{\"error\":\"serviceId, locality, and preferredDate are required\"}");
            return;
        }

        int serviceId = Integer.parseInt(serviceIdStr);
        LocalDate date = DateUtil.parseDate(dateStr);
        if (date == null) {
            res.setStatus(400);
            res.getWriter().write("{\"error\":\"Invalid date format\"}");
            return;
        }

        List<ServiceRequest> matches = matchingService.findCompatibleRequests(serviceId, locality, date);
        res.getWriter().write(JsonUtil.toJson(matches));
    }

    private void handleMyRequests(HttpServletRequest req, HttpServletResponse res) throws IOException {
        int userId = (int) req.getSession().getAttribute("userId");
        String view = req.getParameter("view");

        List<ServiceRequest> list;
        if ("joined".equals(view)) {
            list = requestService.getByMember(userId);
        } else {
            list = requestService.getByCreator(userId);
        }
        res.getWriter().write(JsonUtil.toJson(list));
    }

    private int parseId(String path) {
        try {
            return Integer.parseInt(path.replaceAll("[^0-9]", "").trim());
        } catch (NumberFormatException e) {
            throw new InvalidRequestException("Invalid request ID in path");
        }
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "'");
    }
}
