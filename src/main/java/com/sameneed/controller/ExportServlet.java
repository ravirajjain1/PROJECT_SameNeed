package com.sameneed.controller;

import com.sameneed.service.ExportService;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/api/export/*")
public class ExportServlet extends HttpServlet {

    private final ExportService exportService = new ExportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String role = (String) req.getSession().getAttribute("role");
        if (!"ADMIN".equals(role)) {
            res.setStatus(403);
            res.getWriter().write("{\"error\":\"Admin access required\"}");
            return;
        }

        String path = req.getPathInfo();
        try {
            if ("/bookings".equals(path)) {
                byte[] csv = exportService.exportBookingsCsv(1000);
                res.setContentType("text/csv");
                res.setHeader("Content-Disposition", "attachment; filename=\"bookings_export.csv\"");
                res.setContentLength(csv.length);
                res.getOutputStream().write(csv);
            } else {
                res.setStatus(404);
                res.getWriter().write("{\"error\":\"Export type not found\"}");
            }
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Export failed\"}");
        }
    }
}
