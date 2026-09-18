package com.sameneed.dao;

import com.sameneed.config.DatabaseConfig;
import com.sameneed.enums.OfferStatus;
import com.sameneed.exception.DatabaseException;
import com.sameneed.model.Offer;
import com.sameneed.model.OfferNegotiation;
import com.sameneed.model.OfferResponse;
import com.sameneed.enums.MemberResponseStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OfferDao {

    public int insert(Offer offer) {
        String sql = "INSERT INTO offers (request_id, provider_id, amount_per_member, required_members, valid_until) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, offer.getRequestId());
            ps.setInt(2, offer.getProviderId());
            ps.setBigDecimal(3, offer.getAmountPerMember());
            ps.setInt(4, offer.getRequiredMembers());
            ps.setTimestamp(5, Timestamp.valueOf(offer.getValidUntil()));
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert offer", e);
        }
    }

    public Offer findById(int offerId) {
        String sql = "SELECT o.*, u.display_name AS provider_name FROM offers o " +
                     "JOIN users u ON o.provider_id = u.user_id WHERE o.offer_id=?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find offer by id", e);
        }
        return null;
    }

    public List<Offer> findByRequest(int requestId) {
        String sql = "SELECT o.*, u.display_name AS provider_name FROM offers o " +
                     "JOIN users u ON o.provider_id = u.user_id WHERE o.request_id=? ORDER BY o.created_at DESC";
        List<Offer> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find offers by request", e);
        }
        return list;
    }

    public List<Offer> findByProvider(int providerId) {
        String sql = "SELECT o.*, u.display_name AS provider_name FROM offers o " +
                     "JOIN users u ON o.provider_id = u.user_id WHERE o.provider_id=? ORDER BY o.created_at DESC";
        List<Offer> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, providerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find offers by provider", e);
        }
        return list;
    }

    public List<Offer> findExpiredPending() {
        String sql = "SELECT o.*, u.display_name AS provider_name FROM offers o " +
                     "JOIN users u ON o.provider_id = u.user_id " +
                     "WHERE o.status = 'PENDING' AND o.valid_until < NOW()";
        List<Offer> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find expired pending offers", e);
        }
        return list;
    }

    public void updateStatus(int offerId, OfferStatus status) {
        String sql = "UPDATE offers SET status=? WHERE offer_id=?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, offerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update offer status", e);
        }
    }

    public void updateAmount(int offerId, BigDecimal newAmount) {
        String sql = "UPDATE offers SET amount_per_member=?, status='COUNTERED' WHERE offer_id=?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, newAmount);
            ps.setInt(2, offerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update offer amount", e);
        }
    }

    public int insertNegotiation(OfferNegotiation neg) {
        String sql = "INSERT INTO offer_negotiations (offer_id, sender_role, amount, message) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, neg.getOfferId());
            ps.setString(2, neg.getSenderRole());
            ps.setBigDecimal(3, neg.getAmount());
            ps.setString(4, neg.getMessage());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : -1;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to insert negotiation round", e);
        }
    }

    public List<OfferNegotiation> findNegotiations(int offerId) {
        String sql = "SELECT * FROM offer_negotiations WHERE offer_id=? ORDER BY created_at ASC";
        List<OfferNegotiation> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OfferNegotiation neg = new OfferNegotiation();
                    neg.setNegotiationId(rs.getInt("negotiation_id"));
                    neg.setOfferId(rs.getInt("offer_id"));
                    neg.setSenderRole(rs.getString("sender_role"));
                    neg.setAmount(rs.getBigDecimal("amount"));
                    neg.setMessage(rs.getString("message"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) neg.setCreatedAt(ts.toLocalDateTime());
                    list.add(neg);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find negotiations", e);
        }
        return list;
    }

    public void createResponsesForMembers(int offerId, List<Integer> memberIds) {
        String sql = "INSERT IGNORE INTO offer_responses (offer_id, member_id) VALUES (?,?)";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int memberId : memberIds) {
                ps.setInt(1, offerId);
                ps.setInt(2, memberId);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create offer responses", e);
        }
    }

    public void updateResponse(int offerId, int memberId, MemberResponseStatus status) {
        String sql = "UPDATE offer_responses SET status=?, responded_at=NOW() WHERE offer_id=? AND member_id=?";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, offerId);
            ps.setInt(3, memberId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update offer response", e);
        }
    }

    public List<OfferResponse> findResponses(int offerId) {
        String sql = "SELECT ore.*, rm.anonymous_alias FROM offer_responses ore " +
                     "JOIN request_members rm ON ore.member_id = rm.member_id WHERE ore.offer_id=?";
        List<OfferResponse> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OfferResponse resp = new OfferResponse();
                    resp.setResponseId(rs.getInt("response_id"));
                    resp.setOfferId(rs.getInt("offer_id"));
                    resp.setMemberId(rs.getInt("member_id"));
                    resp.setMemberAlias(rs.getString("anonymous_alias"));
                    resp.setStatus(MemberResponseStatus.valueOf(rs.getString("status")));
                    Timestamp ts = rs.getTimestamp("responded_at");
                    if (ts != null) resp.setRespondedAt(ts.toLocalDateTime());
                    list.add(resp);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find offer responses", e);
        }
        return list;
    }

    public int countAcceptedResponses(int offerId) {
        String sql = "SELECT COUNT(*) FROM offer_responses WHERE offer_id=? AND status='ACCEPTED'";
        try (Connection conn = DatabaseConfig.openConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, offerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count accepted responses", e);
        }
    }

    private Offer mapRow(ResultSet rs) throws SQLException {
        Offer o = new Offer();
        o.setOfferId(rs.getInt("offer_id"));
        o.setRequestId(rs.getInt("request_id"));
        o.setProviderId(rs.getInt("provider_id"));
        o.setProviderName(rs.getString("provider_name"));
        o.setAmountPerMember(rs.getBigDecimal("amount_per_member"));
        o.setRequiredMembers(rs.getInt("required_members"));
        Timestamp validUntil = rs.getTimestamp("valid_until");
        if (validUntil != null) o.setValidUntil(validUntil.toLocalDateTime());
        o.setStatus(OfferStatus.valueOf(rs.getString("status")));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) o.setCreatedAt(ts.toLocalDateTime());
        return o;
    }
}
