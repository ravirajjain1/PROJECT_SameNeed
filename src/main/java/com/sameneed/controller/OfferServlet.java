package com.sameneed.controller;

import com.sameneed.exception.OfferException;
import com.sameneed.model.Offer;
import com.sameneed.model.OfferNegotiation;
import com.sameneed.model.OfferResponse;
import com.sameneed.service.MemberService;
import com.sameneed.service.OfferService;
import com.sameneed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/offers/*")
public class OfferServlet extends HttpServlet {

    private final OfferService offerService = new OfferService();
    private final MemberService memberService = new MemberService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            String path = req.getPathInfo();
            if (path == null || path.equals("/")) {
                String requestIdStr = req.getParameter("requestId");
                if (requestIdStr != null) {
                    List<Offer> offers = offerService.getOffersByRequest(Integer.parseInt(requestIdStr));
                    res.getWriter().write(JsonUtil.toJson(offers));
                } else {
                    int userId = (int) req.getSession().getAttribute("userId");
                    List<Offer> offers = offerService.getOffersByProvider(userId);
                    res.getWriter().write(JsonUtil.toJson(offers));
                }
            } else if (path.endsWith("/negotiations")) {
                int offerId = Integer.parseInt(path.replace("/", "").replace("negotiations", "").replace("-", ""));
                List<OfferNegotiation> history = offerService.getNegotiationHistory(offerId);
                res.getWriter().write(JsonUtil.toJson(history));
            } else if (path.endsWith("/responses")) {
                int offerId = Integer.parseInt(path.replace("/", "").replace("responses", "").replace("-", ""));
                List<OfferResponse> responses = offerService.getMemberResponses(offerId);
                res.getWriter().write(JsonUtil.toJson(responses));
            } else {
                int offerId = Integer.parseInt(path.replaceAll("[^0-9]", "").trim());
                Offer offer = offerService.getOffer(offerId);
                res.getWriter().write(JsonUtil.toJson(offer));
            }
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
            String path = req.getPathInfo();

            Map<String, Object> body = JsonUtil.fromJson(req.getReader(), Map.class);
            if (body == null) throw new OfferException("Invalid request body");

            if (path == null || path.equals("/")) {
                if (!"SERVICE_PROVIDER".equals(role)) {
                    res.setStatus(403);
                    res.getWriter().write("{\"error\":\"Only service providers can submit offers\"}");
                    return;
                }
                int requestId = ((Number) body.get("requestId")).intValue();
                BigDecimal amount = new BigDecimal(body.get("amountPerMember").toString());
                int requiredMembers = body.get("requiredMembers") != null ? ((Number) body.get("requiredMembers")).intValue() : 1;
                int validHours = body.get("validHours") != null ? ((Number) body.get("validHours")).intValue() : 24;

                Offer offer = offerService.submitOffer(requestId, userId, amount, requiredMembers, validHours);
                res.setStatus(201);
                res.getWriter().write(JsonUtil.toJson(offer));

            } else if (path.endsWith("/counter")) {
                int offerId = Integer.parseInt(path.replace("/", "").replace("counter", "").replace("-", "").trim());
                BigDecimal newAmount = new BigDecimal(body.get("amount").toString());
                String message = (String) body.get("message");
                boolean isProvider = "SERVICE_PROVIDER".equals(role);
                OfferNegotiation round = offerService.counterOffer(offerId, userId, newAmount, message, isProvider);
                res.getWriter().write(JsonUtil.toJson(round));

            } else if (path.endsWith("/accept")) {
                int offerId = Integer.parseInt(path.replace("/", "").replace("accept", "").replace("-", "").trim());
                offerService.acceptFinalOffer(offerId, userId);
                res.getWriter().write("{\"message\":\"Offer accepted. Members can now respond.\"}");

            } else if (path.endsWith("/reject")) {
                int offerId = Integer.parseInt(path.replace("/", "").replace("reject", "").replace("-", "").trim());
                offerService.rejectOffer(offerId, userId);
                res.getWriter().write("{\"message\":\"Offer rejected\"}");

            } else if (path.endsWith("/respond")) {
                int offerId = Integer.parseInt(path.replace("/", "").replace("respond", "").replace("-", "").trim());
                boolean accepted = Boolean.TRUE.equals(body.get("accepted"));
                int memberId = ((Number) body.get("memberId")).intValue();
                offerService.respondToOffer(offerId, memberId, accepted);
                res.getWriter().write("{\"message\":\"Response recorded\"}");
            }

        } catch (OfferException e) {
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
