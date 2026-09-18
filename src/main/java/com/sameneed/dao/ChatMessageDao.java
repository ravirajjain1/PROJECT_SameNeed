package com.sameneed.dao;

import com.sameneed.config.DatabaseConfig;
import com.sameneed.exception.DatabaseException;
import com.sameneed.model.ChatMessage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatMessageDao {

    public int insert(ChatMessage msg) {
        String sql = "INSERT INTO chat_messages (request_id, sender_alias, content) VALUES (?,?,?)";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, msg.getRequestId());
            ps.setString(2, msg.getSenderAlias());
            ps.setString(3, msg.getContent());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                msg.setMessageId(keys.getInt(1));
                return keys.getInt(1);
            }
            return -1;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert chat message", e);
        }
    }

    public List<ChatMessage> findByRequest(int requestId) {
        String sql = "SELECT * FROM chat_messages WHERE request_id=? ORDER BY sent_at ASC";
        List<ChatMessage> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChatMessage m = new ChatMessage();
                    m.setMessageId(rs.getInt("message_id"));
                    m.setRequestId(rs.getInt("request_id"));
                    m.setSenderAlias(rs.getString("sender_alias"));
                    m.setContent(rs.getString("content"));
                    Timestamp ts = rs.getTimestamp("sent_at");
                    if (ts != null) m.setSentAt(ts.toLocalDateTime());
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find chat messages", e);
        }
        return list;
    }
}
