package com.sameneed.controller;

import com.sameneed.exception.InvalidRequestException;
import com.sameneed.model.Review;
import com.sameneed.service.ReviewService;
import com.sameneed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/reviews/*")
public class ReviewServlet extends HttpServlet {

    private final ReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            String providerIdStr = req.getParameter("providerId");
            if (providerIdStr == null) {
                res.setStatus(400);
                res.getWriter().write("{\"error\":\"providerId is required\"}");
                return;
            }
            List<Review> reviews = reviewService.getByProvider(Integer.parseInt(providerIdStr));
            res.getWriter().write(JsonUtil.toJson(reviews));
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Failed to load reviews\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            int userId = (int) req.getSession().getAttribute("userId");
            Map<String, Object> body = JsonUtil.fromJson(req.getReader(), Map.class);
            if (body == null) throw new InvalidRequestException("Invalid request body");

            int bookingId = ((Number) body.get("bookingId")).intValue();
            int rating = ((Number) body.get("rating")).intValue();
            String comment = (String) body.get("comment");

            Review review = reviewService.submitReview(bookingId, userId, rating, comment);
            res.setStatus(201);
            res.getWriter().write(JsonUtil.toJson(review));

        } catch (InvalidRequestException e) {
            res.setStatus(400);
            res.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Failed to submit review\"}");
        }
    }

    private String escape(String s) {
        return s == null ? "" : s.replace("\"", "'");
    }
}
