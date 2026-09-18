package com.sameneed.dao;

import com.sameneed.config.DatabaseConfig;
import com.sameneed.enums.BookingStatus;
import com.sameneed.exception.DatabaseException;
import com.sameneed.model.Booking;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDao {

    private static final String SELECT_BASE =
            "SELECT b.*, sp_user.display_name AS provider_name, s.name AS service_name, sr.locality " +
            "FROM bookings b " +
            "JOIN users sp_user ON b.provider_id = sp_user.user_id " +
            "JOIN service_requests sr ON b.request_id = sr.request_id " +
            "JOIN services s ON sr.service_id = s.service_id ";

    public int insert(Booking booking) {
        String sql = "INSERT INTO bookings (request_id, offer_id, provider_id, scheduled_date, scheduled_time) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, booking.getRequestId());
            ps.setInt(2, booking.getOfferId());
            ps.setInt(3, booking.getProviderId());
            if (booking.getScheduledDate() != null) {
                ps.setDate(4, Date.valueOf(booking.getScheduledDate()));
            } else {
                ps.setNull(4, Types.DATE);
            }
            if (booking.getScheduledTime() != null) {
                ps.setTime(5, Time.valueOf(booking.getScheduledTime()));
            } else {
                ps.setNull(5, Types.TIME);
            }
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert booking", e);
        }
    }

    public Booking findById(int bookingId) {
        String sql = SELECT_BASE + "WHERE b.booking_id=?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find booking by id", e);
        }
        return null;
    }

    public Booking findByRequest(int requestId) {
        String sql = SELECT_BASE + "WHERE b.request_id=?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find booking by request", e);
        }
        return null;
    }

    public List<Booking> findByProvider(int providerId) {
        String sql = SELECT_BASE + "WHERE b.provider_id=? ORDER BY b.created_at DESC";
        List<Booking> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, providerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find bookings by provider", e);
        }
        return list;
    }

    public List<Booking> findByUser(int userId) {
        String sql = SELECT_BASE +
                "JOIN request_members rm ON b.request_id = rm.request_id " +
                "WHERE rm.user_id=? AND rm.is_active=1 ORDER BY b.created_at DESC";
        List<Booking> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find bookings by user", e);
        }
        return list;
    }

    public List<Booking> findAll(int limit, int offset) {
        String sql = SELECT_BASE + "ORDER BY b.created_at DESC LIMIT ? OFFSET ?";
        List<Booking> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list all bookings", e);
        }
        return list;
    }

    public void updateStatus(int bookingId, BookingStatus status) {
        String sql = "UPDATE bookings SET status=? WHERE booking_id=?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update booking status", e);
        }
    }

    public int countAll() {
        String sql = "SELECT COUNT(*) FROM bookings";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count bookings", e);
        }
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id"));
        b.setRequestId(rs.getInt("request_id"));
        b.setOfferId(rs.getInt("offer_id"));
        b.setProviderId(rs.getInt("provider_id"));
        b.setProviderName(rs.getString("provider_name"));
        b.setServiceName(rs.getString("service_name"));
        b.setLocality(rs.getString("locality"));
        Date d = rs.getDate("scheduled_date");
        if (d != null) b.setScheduledDate(d.toLocalDate());
        Time t = rs.getTime("scheduled_time");
        if (t != null) b.setScheduledTime(t.toLocalTime());
        b.setStatus(BookingStatus.valueOf(rs.getString("status")));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) b.setCreatedAt(ts.toLocalDateTime());
        return b;
    }
}
