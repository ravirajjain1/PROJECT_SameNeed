package com.sameneed.dao;

import com.sameneed.config.DatabaseConfig;
import com.sameneed.exception.DatabaseException;
import com.sameneed.model.RequestMember;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RequestMemberDao {

    public int insert(RequestMember member) {
        String sql = "INSERT INTO request_members (request_id, user_id, anonymous_alias) VALUES (?,?,?)";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, member.getRequestId());
            ps.setInt(2, member.getUserId());
            ps.setString(3, member.getAnonymousAlias());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert request member", e);
        }
    }

    public boolean isMember(int requestId, int userId) {
        String sql = "SELECT 1 FROM request_members WHERE request_id=? AND user_id=? AND is_active=1";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check membership", e);
        }
    }

    public RequestMember findByRequestAndUser(int requestId, int userId) {
        String sql = "SELECT * FROM request_members WHERE request_id=? AND user_id=? AND is_active=1";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find member", e);
        }
        return null;
    }

    public List<RequestMember> findByRequest(int requestId) {
        String sql = "SELECT * FROM request_members WHERE request_id=? AND is_active=1 ORDER BY joined_at ASC";
        List<RequestMember> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list request members", e);
        }
        return list;
    }

    public int countActiveMembers(int requestId) {
        String sql = "SELECT COUNT(*) FROM request_members WHERE request_id=? AND is_active=1";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count active members", e);
        }
    }

    public void deactivate(int memberId) {
        String sql = "UPDATE request_members SET is_active=0 WHERE member_id=?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, memberId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to deactivate member", e);
        }
    }

    private RequestMember mapRow(ResultSet rs) throws SQLException {
        RequestMember m = new RequestMember();
        m.setMemberId(rs.getInt("member_id"));
        m.setRequestId(rs.getInt("request_id"));
        m.setUserId(rs.getInt("user_id"));
        m.setAnonymousAlias(rs.getString("anonymous_alias"));
        m.setActive(rs.getBoolean("is_active"));
        Timestamp ts = rs.getTimestamp("joined_at");
        if (ts != null) m.setJoinedAt(ts.toLocalDateTime());
        return m;
    }
}
