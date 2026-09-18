package com.sameneed.controller;

import com.sameneed.exception.InvalidRequestException;
import com.sameneed.model.Report;
import com.sameneed.service.ReportService;
import com.sameneed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/reports/*")
public class ReportServlet extends HttpServlet {

    private final ReportService reportService = new ReportService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            int userId = (int) req.getSession().getAttribute("userId");
            Map<String, Object> body = JsonUtil.fromJson(req.getReader(), Map.class);
            if (body == null) throw new InvalidRequestException("Invalid request body");

            String targetType = (String) body.get("targetType");
            int targetId = ((Number) body.get("targetId")).intValue();
            String reason = (String) body.get("reason");

            Report report = reportService.fileReport(userId, targetType, targetId, reason);
            res.setStatus(201);
            res.getWriter().write(JsonUtil.toJson(report));

        } catch (InvalidRequestException e) {
            res.setStatus(400);
            res.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Failed to file report\"}");
        }
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "'");
    }
}
