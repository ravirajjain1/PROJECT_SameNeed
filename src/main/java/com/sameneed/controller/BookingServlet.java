package com.sameneed.controller;

import com.sameneed.exception.BookingException;
import com.sameneed.enums.BookingStatus;
import com.sameneed.model.Booking;
import com.sameneed.service.BookingService;
import com.sameneed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/bookings/*")
public class BookingServlet extends HttpServlet {

    private final BookingService bookingService = new BookingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            int userId = (int) req.getSession().getAttribute("userId");
            String role = (String) req.getSession().getAttribute("role");
            String path = req.getPathInfo();

            if (path == null || path.equals("/")) {
                List<Booking> bookings;
                if ("SERVICE_PROVIDER".equals(role)) {
                    bookings = bookingService.getByProvider(userId);
                } else {
                    bookings = bookingService.getByUser(userId);
                }
                res.getWriter().write(JsonUtil.toJson(bookings));
            } else {
                int bookingId = Integer.parseInt(path.replaceAll("[^0-9]", "").trim());
                Booking booking = bookingService.getById(bookingId);
                res.getWriter().write(JsonUtil.toJson(booking));
            }
        } catch (BookingException e) {
            res.setStatus(404);
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

            if (!"SERVICE_PROVIDER".equals(role)) {
                res.setStatus(403);
                res.getWriter().write("{\"error\":\"Only service providers can confirm bookings\"}");
                return;
            }

            Map<String, Object> body = JsonUtil.fromJson(req.getReader(), Map.class);
            if (body == null) throw new BookingException("Invalid request body");

            int offerId = ((Number) body.get("offerId")).intValue();
            Booking booking = bookingService.confirmBooking(offerId, userId);
            res.setStatus(201);
            res.getWriter().write(JsonUtil.toJson(booking));

        } catch (BookingException e) {
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
            String role = (String) req.getSession().getAttribute("role");

            if (!"SERVICE_PROVIDER".equals(role)) {
                res.setStatus(403);
                res.getWriter().write("{\"error\":\"Only service providers can update booking status\"}");
                return;
            }

            String path = req.getPathInfo();
            int bookingId = Integer.parseInt(path.replaceAll("[^0-9]", "").trim());
            Map<String, Object> body = JsonUtil.fromJson(req.getReader(), Map.class);

            String statusStr = (String) body.get("status");
            BookingStatus newStatus = BookingStatus.valueOf(statusStr.toUpperCase());
            bookingService.updateStatus(bookingId, newStatus, userId);

            res.getWriter().write("{\"message\":\"Booking status updated to " + newStatus + "\"}");

        } catch (IllegalArgumentException e) {
            res.setStatus(400);
            res.getWriter().write("{\"error\":\"Invalid booking status\"}");
        } catch (BookingException e) {
            res.setStatus(400);
            res.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Internal server error\"}");
        }
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "'");
    }
}
