package com.sameneed.controller;

import com.sameneed.model.ChatMessage;
import com.sameneed.service.MemberService;
import com.sameneed.dao.ChatMessageDao;
import com.sameneed.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/chat/*")
public class ChatServlet extends HttpServlet {

    private final ChatMessageDao chatMessageDao = new ChatMessageDao();
    private final MemberService memberService = new MemberService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
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
            if (!memberService.isMember(requestId, userId)) {
                res.setStatus(403);
                res.getWriter().write("{\"error\":\"You are not a member of this group\"}");
                return;
            }
            List<ChatMessage> messages = chatMessageDao.findByRequest(requestId);
            res.getWriter().write(JsonUtil.toJson(messages));
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"Failed to load chat history\"}");
        }
    }
}
