package com.sameneed.controller;

import com.sameneed.exception.InvalidRequestException;
import com.sameneed.model.RequestMember;
import com.sameneed.service.MemberService;
import com.sameneed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/members/*")
public class MemberServlet extends HttpServlet {

    private final MemberService memberService = new MemberService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            String requestIdStr = req.getParameter("requestId");
            if (requestIdStr == null) {
                res.setStatus(400);
                res.getWriter().write("{\"error\":\"requestId parameter is required\"}");
                return;
            }
            int requestId = Integer.parseInt(requestIdStr);
            List<RequestMember> members = memberService.getMembers(requestId);
            res.getWriter().write(JsonUtil.toJson(members));
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
                res.getWriter().write("{\"error\":\"Only customers can join requests\"}");
                return;
            }
            String path = req.getPathInfo();
            int requestId = Integer.parseInt(path.replaceAll("[^0-9]", "").trim());

            RequestMember member = memberService.joinRequest(requestId, userId);
            res.setStatus(201);
            res.getWriter().write(JsonUtil.toJson(member));
        } catch (InvalidRequestException e) {
            res.setStatus(400);
            res.getWriter().write("{\"error\":\"" + escape(e.getMessage()) + "\"}");
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Internal server error\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        try {
            int userId = (int) req.getSession().getAttribute("userId");
            String requestIdStr = req.getParameter("requestId");
            if (requestIdStr == null) {
                res.setStatus(400);
                res.getWriter().write("{\"error\":\"requestId is required\"}");
                return;
            }
            int requestId = Integer.parseInt(requestIdStr);
            memberService.leaveRequest(requestId, userId);
            res.getWriter().write("{\"message\":\"You have left the group\"}");
        } catch (InvalidRequestException e) {
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
