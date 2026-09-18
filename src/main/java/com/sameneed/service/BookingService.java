package com.sameneed.service;

import com.sameneed.dao.BookingDao;
import com.sameneed.dao.OfferDao;
import com.sameneed.dao.RequestDao;
import com.sameneed.enums.BookingStatus;
import com.sameneed.enums.OfferStatus;
import com.sameneed.enums.RequestStatus;
import com.sameneed.exception.BookingException;
import com.sameneed.model.Booking;
import com.sameneed.model.Offer;
import com.sameneed.model.ServiceRequest;

import java.util.List;

public class BookingService {

    private final BookingDao bookingDao = new BookingDao();
    private final OfferDao offerDao = new OfferDao();
    private final RequestDao requestDao = new RequestDao();
    private final NotificationService notificationService = new NotificationService();

    public Booking confirmBooking(int offerId, int providerId) {
        Offer offer = offerDao.findById(offerId);
        if (offer == null) throw new BookingException("Offer not found");
        if (offer.getProviderId() != providerId) {
            throw new BookingException("You are not the provider for this offer");
        }
        if (offer.getStatus() != OfferStatus.ACCEPTED) {
            throw new BookingException("Offer must be in ACCEPTED status before booking can be confirmed");
        }
        if (offer.isExpired()) throw new BookingException("This offer has expired");

        ServiceRequest req = requestDao.findById(offer.getRequestId());
        if (req == null) throw new BookingException("Request not found");

        Booking booking = new Booking();
        booking.setRequestId(offer.getRequestId());
        booking.setOfferId(offerId);
        booking.setProviderId(providerId);
        booking.setScheduledDate(req.getPreferredDate());
        booking.setScheduledTime(req.getPreferredTime());
        booking.setStatus(BookingStatus.CONFIRMED);

        int bookingId = bookingDao.insert(booking);
        booking.setBookingId(bookingId);

        requestDao.updateStatus(offer.getRequestId(), RequestStatus.BOOKED);
        notificationService.queueForUser(req.getCreatorId(),
                "Your booking has been confirmed! Provider has accepted the service request.");

        return booking;
    }

    public void updateStatus(int bookingId, BookingStatus newStatus, int providerId) {
        Booking booking = bookingDao.findById(bookingId);
        if (booking == null) throw new BookingException("Booking not found");
        if (booking.getProviderId() != providerId) {
            throw new BookingException("You are not the provider for this booking");
        }

        validateStatusTransition(booking.getStatus(), newStatus);
        bookingDao.updateStatus(bookingId, newStatus);

        if (newStatus == BookingStatus.IN_PROGRESS) {
            requestDao.updateStatus(booking.getRequestId(), RequestStatus.IN_PROGRESS);
        } else if (newStatus == BookingStatus.COMPLETED) {
            requestDao.updateStatus(booking.getRequestId(), RequestStatus.COMPLETED);
        }
    }

    private void validateStatusTransition(BookingStatus current, BookingStatus next) {
        if (current == BookingStatus.CONFIRMED && next == BookingStatus.IN_PROGRESS) return;
        if (current == BookingStatus.IN_PROGRESS && next == BookingStatus.COMPLETED) return;
        if (next == BookingStatus.CANCELLED) return;
        throw new BookingException("Invalid status transition from " + current + " to " + next);
    }

    public Booking getById(int bookingId) {
        Booking b = bookingDao.findById(bookingId);
        if (b == null) throw new BookingException("Booking not found");
        return b;
    }

    public Booking getByRequest(int requestId) {
        return bookingDao.findByRequest(requestId);
    }

    public List<Booking> getByUser(int userId) {
        return bookingDao.findByUser(userId);
    }

    public List<Booking> getByProvider(int providerId) {
        return bookingDao.findByProvider(providerId);
    }

    public List<Booking> getAll(int limit, int offset) {
        return bookingDao.findAll(limit, offset);
    }

    public int countAll() {
        return bookingDao.countAll();
    }
}
