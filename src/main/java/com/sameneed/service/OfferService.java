package com.sameneed.service;

import com.sameneed.dao.OfferDao;
import com.sameneed.dao.RequestDao;
import com.sameneed.dao.RequestMemberDao;
import com.sameneed.enums.MemberResponseStatus;
import com.sameneed.enums.OfferStatus;
import com.sameneed.enums.RequestStatus;
import com.sameneed.exception.OfferException;
import com.sameneed.model.Offer;
import com.sameneed.model.OfferNegotiation;
import com.sameneed.model.OfferResponse;
import com.sameneed.model.RequestMember;
import com.sameneed.model.ServiceRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OfferService {

    private final OfferDao offerDao = new OfferDao();
    private final RequestDao requestDao = new RequestDao();
    private final RequestMemberDao memberDao = new RequestMemberDao();
    private final NotificationService notificationService = new NotificationService();

    public Offer submitOffer(int requestId, int providerId, BigDecimal amount, int requiredMembers, int validHours) {
        ServiceRequest req = requestDao.findById(requestId);
        if (req == null) throw new OfferException("Request not found");
        if (req.getStatus() == RequestStatus.BOOKED || req.getStatus() == RequestStatus.COMPLETED) {
            throw new OfferException("This request has already been booked");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OfferException("Offer amount must be greater than zero");
        }
        if (validHours < 1 || validHours > 72) {
            throw new OfferException("Offer validity must be between 1 and 72 hours");
        }

        Offer offer = new Offer();
        offer.setRequestId(requestId);
        offer.setProviderId(providerId);
        offer.setAmountPerMember(amount);
        offer.setRequiredMembers(requiredMembers > 0 ? requiredMembers : 1);
        offer.setValidUntil(LocalDateTime.now().plusHours(validHours));
        offer.setStatus(OfferStatus.PENDING);

        int offerId = offerDao.insert(offer);
        offer.setOfferId(offerId);

        OfferNegotiation firstRound = new OfferNegotiation();
        firstRound.setOfferId(offerId);
        firstRound.setSenderRole("PROVIDER");
        firstRound.setAmount(amount);
        firstRound.setMessage("Initial offer: ₹" + amount + " per member");
        offerDao.insertNegotiation(firstRound);

        requestDao.updateStatus(requestId, RequestStatus.OFFER_RECEIVED);
        notificationService.queueForUser(req.getCreatorId(),
                "New offer received: ₹" + amount + " per member for your group request.");

        return offer;
    }

    public OfferNegotiation counterOffer(int offerId, int userId, BigDecimal newAmount, String message, boolean isProvider) {
        Offer offer = offerDao.findById(offerId);
        if (offer == null) throw new OfferException("Offer not found");
        if (offer.isExpired()) throw new OfferException("This offer has expired");
        if (offer.getStatus() == OfferStatus.ACCEPTED || offer.getStatus() == OfferStatus.REJECTED) {
            throw new OfferException("This offer is already finalised");
        }
        if (newAmount == null || newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OfferException("Counter offer amount must be greater than zero");
        }

        String senderRole = isProvider ? "PROVIDER" : "COORDINATOR";
        offerDao.updateAmount(offerId, newAmount);

        OfferNegotiation round = new OfferNegotiation();
        round.setOfferId(offerId);
        round.setSenderRole(senderRole);
        round.setAmount(newAmount);
        round.setMessage(message != null ? message.trim() : "");
        offerDao.insertNegotiation(round);

        requestDao.updateStatus(offer.getRequestId(), RequestStatus.NEGOTIATING);

        return round;
    }

    public void acceptFinalOffer(int offerId, int creatorId) {
        Offer offer = offerDao.findById(offerId);
        if (offer == null) throw new OfferException("Offer not found");
        if (offer.isExpired()) throw new OfferException("This offer has expired");

        ServiceRequest req = requestDao.findById(offer.getRequestId());
        if (req == null || req.getCreatorId() != creatorId) {
            throw new OfferException("Only the group creator can accept a final offer");
        }

        offerDao.updateStatus(offerId, OfferStatus.ACCEPTED);
        requestDao.updateStatus(offer.getRequestId(), RequestStatus.OFFER_ACCEPTED);

        List<RequestMember> members = memberDao.findByRequest(offer.getRequestId());
        List<Integer> memberIds = members.stream()
                .map(RequestMember::getMemberId)
                .collect(Collectors.toList());
        offerDao.createResponsesForMembers(offerId, memberIds);

        for (RequestMember member : members) {
            notificationService.queueForUser(member.getUserId(),
                    "The group coordinator has accepted an offer. Please confirm your response.");
        }
    }

    public void respondToOffer(int offerId, int memberId, boolean accepted) {
        MemberResponseStatus status = accepted ? MemberResponseStatus.ACCEPTED : MemberResponseStatus.DECLINED;
        offerDao.updateResponse(offerId, memberId, status);
    }

    public void rejectOffer(int offerId, int creatorId) {
        Offer offer = offerDao.findById(offerId);
        if (offer == null) throw new OfferException("Offer not found");

        ServiceRequest req = requestDao.findById(offer.getRequestId());
        if (req == null || req.getCreatorId() != creatorId) {
            throw new OfferException("Only the group creator can reject an offer");
        }

        offerDao.updateStatus(offerId, OfferStatus.REJECTED);
        requestDao.updateStatus(offer.getRequestId(), RequestStatus.PROVIDER_CONTACTED);
    }

    public List<Offer> getOffersByRequest(int requestId) {
        return offerDao.findByRequest(requestId);
    }

    public List<Offer> getOffersByProvider(int providerId) {
        return offerDao.findByProvider(providerId);
    }

    public Offer getOffer(int offerId) {
        Offer offer = offerDao.findById(offerId);
        if (offer == null) throw new OfferException("Offer not found");
        return offer;
    }

    public List<OfferNegotiation> getNegotiationHistory(int offerId) {
        return offerDao.findNegotiations(offerId);
    }

    public List<OfferResponse> getMemberResponses(int offerId) {
        return offerDao.findResponses(offerId);
    }

    public int countAcceptedResponses(int offerId) {
        return offerDao.countAcceptedResponses(offerId);
    }

    public void expireOffer(int offerId) {
        offerDao.updateStatus(offerId, OfferStatus.EXPIRED);
    }
}
