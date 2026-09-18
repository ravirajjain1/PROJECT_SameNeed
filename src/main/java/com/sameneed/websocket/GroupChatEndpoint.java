package com.sameneed.websocket;

import com.sameneed.dao.ChatMessageDao;
import com.sameneed.dao.RequestMemberDao;
import com.sameneed.model.ChatMessage;
import com.sameneed.model.RequestMember;
import com.sameneed.util.JsonUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/ws/chat/{requestId}", configurator = HttpSessionConfigurator.class)
public class GroupChatEndpoint {

    private static final Map<Integer, Set<Session>> rooms = new ConcurrentHashMap<>();

    private final ChatMessageDao chatMessageDao = new ChatMessageDao();
    private final RequestMemberDao memberDao = new RequestMemberDao();

    @OnOpen
    public void onOpen(Session session, @PathParam("requestId") int requestId) {
        Integer userId = (Integer) session.getUserProperties().get("userId");
        if (userId == null) {
            closeSession(session, "Authentication required");
            return;
        }

        RequestMember member = memberDao.findByRequestAndUser(requestId, userId);
        if (member == null) {
            closeSession(session, "You are not a member of this group");
            return;
        }

        session.getUserProperties().put("alias", member.getAnonymousAlias());
        session.getUserProperties().put("requestId", requestId);

        rooms.computeIfAbsent(requestId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        Integer requestId = (Integer) session.getUserProperties().get("requestId");
        String alias = (String) session.getUserProperties().get("alias");

        if (requestId == null || alias == null) return;

        Map<String, Object> incomingData = JsonUtil.fromJson(message, Map.class);
        if (incomingData == null) return;

        String content = (String) incomingData.get("content");
        if (content == null || content.isBlank()) return;

        ChatMessage chatMessage = new ChatMessage(requestId, alias, content.trim());
        chatMessageDao.insert(chatMessage);

        Map<String, Object> broadcast = new ConcurrentHashMap<>();
        broadcast.put("messageId", chatMessage.getMessageId());
        broadcast.put("senderAlias", alias);
        broadcast.put("content", chatMessage.getContent());
        broadcast.put("sentAt", chatMessage.getSentAt() != null ? chatMessage.getSentAt().toString() : "");

        String json = JsonUtil.toJson(broadcast);
        Set<Session> room = rooms.get(requestId);
        if (room != null) {
            for (Session peer : room) {
                if (peer.isOpen()) {
                    try {
                        peer.getBasicRemote().sendText(json);
                    } catch (IOException e) {
                        room.remove(peer);
                    }
                }
            }
        }
    }

    @OnClose
    public void onClose(Session session) {
        Integer requestId = (Integer) session.getUserProperties().get("requestId");
        if (requestId != null) {
            Set<Session> room = rooms.get(requestId);
            if (room != null) {
                room.remove(session);
                if (room.isEmpty()) {
                    rooms.remove(requestId);
                }
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        onClose(session);
    }

    private void closeSession(Session session, String reason) {
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, reason));
        } catch (IOException ignored) {}
    }
}
