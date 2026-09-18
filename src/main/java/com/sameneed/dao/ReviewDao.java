package com.sameneed.dao;

import com.sameneed.config.DatabaseConfig;
import com.sameneed.exception.DatabaseException;
import com.sameneed.model.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDao {

    public int insert(Review review) {
        String sql = "INSERT INTO reviews (booking_id, reviewer_id, provider_id, rating, comment) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, review.getBookingId());
            ps.setInt(2, review.getReviewerId());
            ps.setInt(3, review.getProviderId());
            ps.setInt(4, review.getRating());
            ps.setString(5, review.getComment());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert review", e);
        }
    }

    public boolean hasReviewed(int bookingId, int reviewerId) {
        String sql = "SELECT 1 FROM reviews WHERE booking_id=? AND reviewer_id=?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, reviewerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check if reviewed", e);
        }
    }

    public List<Review> findByProvider(int providerId) {
        String sql = "SELECT r.*, u.display_name AS reviewer_name FROM reviews r " +
                     "JOIN users u ON r.reviewer_id = u.user_id " +
                     "WHERE r.provider_id=? ORDER BY r.created_at DESC";
        List<Review> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, providerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find reviews by provider", e);
        }
        return list;
    }

    public double calcAvgRating(int providerId) {
        String sql = "SELECT AVG(rating) FROM reviews WHERE provider_id=?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, providerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double avg = rs.getDouble(1);
                    return rs.wasNull() ? 0.0 : avg;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to calculate avg rating", e);
        }
        return 0.0;
    }

    private Review mapRow(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setReviewId(rs.getInt("review_id"));
        r.setBookingId(rs.getInt("booking_id"));
        r.setReviewerId(rs.getInt("reviewer_id"));
        r.setReviewerName(rs.getString("reviewer_name"));
        r.setProviderId(rs.getInt("provider_id"));
        r.setRating(rs.getInt("rating"));
        r.setComment(rs.getString("comment"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        return r;
    }
}
