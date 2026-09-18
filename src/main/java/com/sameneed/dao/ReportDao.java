package com.sameneed.dao;

import com.sameneed.config.DatabaseConfig;
import com.sameneed.enums.ReportStatus;
import com.sameneed.exception.DatabaseException;
import com.sameneed.model.Report;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDao {

    public int insert(Report report) {
        String sql = "INSERT INTO reports (reporter_id, target_type, target_id, reason) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, report.getReporterId());
            ps.setString(2, report.getTargetType());
            ps.setInt(3, report.getTargetId());
            ps.setString(4, report.getReason());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert report", e);
        }
    }

    public List<Report> findAll() {
        String sql = "SELECT r.*, u.display_name AS reporter_name FROM reports r " +
                     "JOIN users u ON r.reporter_id = u.user_id ORDER BY r.created_at DESC";
        List<Report> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list reports", e);
        }
        return list;
    }

    public void updateStatus(int reportId, ReportStatus status) {
        String sql = "UPDATE reports SET status=? WHERE report_id=?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, reportId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update report status", e);
        }
    }

    public int countOpen() {
        String sql = "SELECT COUNT(*) FROM reports WHERE status='OPEN'";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count open reports", e);
        }
    }

    private Report mapRow(ResultSet rs) throws SQLException {
        Report r = new Report();
        r.setReportId(rs.getInt("report_id"));
        r.setReporterId(rs.getInt("reporter_id"));
        r.setReporterName(rs.getString("reporter_name"));
        r.setTargetType(rs.getString("target_type"));
        r.setTargetId(rs.getInt("target_id"));
        r.setReason(rs.getString("reason"));
        r.setStatus(ReportStatus.valueOf(rs.getString("status")));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) r.setCreatedAt(ts.toLocalDateTime());
        return r;
    }
}
