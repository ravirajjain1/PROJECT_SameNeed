package com.sameneed.dao;

import com.sameneed.config.DatabaseConfig;
import com.sameneed.enums.RequestStatus;
import com.sameneed.exception.DatabaseException;
import com.sameneed.model.ServiceRequest;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RequestDao {

    private static final String SELECT_BASE =
            "SELECT sr.*, u.display_name AS creator_name, s.name AS service_name, sc.name AS category_name " +
            "FROM service_requests sr " +
            "JOIN users u ON sr.creator_id = u.user_id " +
            "JOIN services s ON sr.service_id = s.service_id " +
            "JOIN service_categories sc ON s.category_id = sc.category_id ";

    public int insert(ServiceRequest req) {
        String sql = "INSERT INTO service_requests " +
                "(creator_id, service_id, problem_description, locality, preferred_date, preferred_time, " +
                "budget_expectation, max_group_size, current_member_count, status) VALUES (?,?,?,?,?,?,?,?,1,'REQUESTED')";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, req.getCreatorId());
            ps.setInt(2, req.getServiceId());
            ps.setString(3, req.getProblemDescription());
            ps.setString(4, req.getLocality());
            ps.setDate(5, Date.valueOf(req.getPreferredDate()));
            ps.setTime(6, Time.valueOf(req.getPreferredTime()));
            if (req.getBudgetExpectation() != null) {
                ps.setBigDecimal(7, req.getBudgetExpectation());
            } else {
                ps.setNull(7, Types.DECIMAL);
            }
            ps.setInt(8, req.getMaxGroupSize());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert service request", e);
        }
    }

    public ServiceRequest findById(int requestId) {
        String sql = SELECT_BASE + "WHERE sr.request_id = ?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find request by id", e);
        }
        return null;
    }

    public List<ServiceRequest> findCompatible(int serviceId, String locality, LocalDate date) {
        String sql = SELECT_BASE +
                "WHERE sr.service_id = ? " +
                "AND LOWER(sr.locality) LIKE LOWER(?) " +
                "AND sr.preferred_date = ? " +
                "AND sr.status IN ('REQUESTED','GROUP_FORMING') " +
                "AND sr.current_member_count < sr.max_group_size " +
                "ORDER BY sr.created_at ASC";
        List<ServiceRequest> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, serviceId);
            ps.setString(2, "%" + locality.trim() + "%");
            ps.setDate(3, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find compatible requests", e);
        }
        return list;
    }

    public List<ServiceRequest> findByCreator(int creatorId) {
        String sql = SELECT_BASE + "WHERE sr.creator_id = ? ORDER BY sr.created_at DESC";
        List<ServiceRequest> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, creatorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find requests by creator", e);
        }
        return list;
    }

    public List<ServiceRequest> findByMember(int userId) {
        String sql = SELECT_BASE +
                "JOIN request_members rm ON sr.request_id = rm.request_id " +
                "WHERE rm.user_id = ? AND rm.is_active = 1 " +
                "ORDER BY sr.created_at DESC";
        List<ServiceRequest> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find requests by member", e);
        }
        return list;
    }

    public List<ServiceRequest> findAllOpen() {
        String sql = SELECT_BASE +
                "WHERE sr.status IN ('REQUESTED','GROUP_FORMING') " +
                "ORDER BY sr.created_at DESC LIMIT 50";
        List<ServiceRequest> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list open requests", e);
        }
        return list;
    }

    public List<ServiceRequest> findEligibleForProvider(int providerId) {
        String sql = SELECT_BASE +
                "JOIN provider_services ps ON sr.service_id = ps.service_id " +
                "WHERE ps.provider_id = ? " +
                "AND sr.status IN ('PROVIDER_CONTACTED','GROUP_FORMING') " +
                "ORDER BY sr.preferred_date ASC";
        List<ServiceRequest> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, providerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find eligible requests for provider", e);
        }
        return list;
    }

    public List<ServiceRequest> findAll(int limit, int offset) {
        String sql = SELECT_BASE + "ORDER BY sr.created_at DESC LIMIT ? OFFSET ?";
        List<ServiceRequest> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list all requests", e);
        }
        return list;
    }

    public void updateStatus(int requestId, RequestStatus status) {
        String sql = "UPDATE service_requests SET status=? WHERE request_id=?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, requestId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update request status", e);
        }
    }

    public void incrementMemberCount(int requestId) {
        String sql = "UPDATE service_requests SET current_member_count = current_member_count + 1 WHERE request_id=?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to increment member count", e);
        }
    }

    public void decrementMemberCount(int requestId) {
        String sql = "UPDATE service_requests SET current_member_count = GREATEST(1, current_member_count - 1) WHERE request_id=?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to decrement member count", e);
        }
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM service_requests";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count requests", e);
        }
    }

    private ServiceRequest mapRow(ResultSet rs) throws SQLException {
        ServiceRequest req = new ServiceRequest();
        req.setRequestId(rs.getInt("request_id"));
        req.setCreatorId(rs.getInt("creator_id"));
        req.setCreatorName(rs.getString("creator_name"));
        req.setServiceId(rs.getInt("service_id"));
        req.setServiceName(rs.getString("service_name"));
        req.setCategoryName(rs.getString("category_name"));
        req.setProblemDescription(rs.getString("problem_description"));
        req.setLocality(rs.getString("locality"));
        Date d = rs.getDate("preferred_date");
        if (d != null) req.setPreferredDate(d.toLocalDate());
        Time t = rs.getTime("preferred_time");
        if (t != null) req.setPreferredTime(t.toLocalTime());
        BigDecimal budget = rs.getBigDecimal("budget_expectation");
        req.setBudgetExpectation(budget);
        req.setMaxGroupSize(rs.getInt("max_group_size"));
        req.setCurrentMemberCount(rs.getInt("current_member_count"));
        req.setStatus(RequestStatus.valueOf(rs.getString("status")));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) req.setCreatedAt(ts.toLocalDateTime());
        return req;
    }
}
