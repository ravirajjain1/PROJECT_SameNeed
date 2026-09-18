package com.sameneed.service;

import com.sameneed.dao.BookingDao;
import com.sameneed.dao.ReviewDao;
import com.sameneed.dao.UserDao;
import com.sameneed.exception.InvalidRequestException;
import com.sameneed.model.Booking;
import com.sameneed.model.Review;
import com.sameneed.enums.BookingStatus;

import java.util.List;

public class ReviewService {

    private final ReviewDao reviewDao = new ReviewDao();
    private final BookingDao bookingDao = new BookingDao();
    private final UserDao userDao = new UserDao();

    public Review submitReview(int bookingId, int reviewerId, int rating, String comment) {
        Booking booking = bookingDao.findById(bookingId);
        if (booking == null) throw new InvalidRequestException("Booking not found");
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new InvalidRequestException("You can only review a completed booking");
        }
        if (rating < 1 || rating > 5) {
            throw new InvalidRequestException("Rating must be between 1 and 5");
        }
        if (reviewDao.hasReviewed(bookingId, reviewerId)) {
            throw new InvalidRequestException("You have already submitted a review for this booking");
        }

        Review review = new Review();
        review.setBookingId(bookingId);
        review.setReviewerId(reviewerId);
        review.setProviderId(booking.getProviderId());
        review.setRating(rating);
        review.setComment(comment != null ? comment.trim() : null);

        int reviewId = reviewDao.insert(review);
        review.setReviewId(reviewId);

        double newAvg = reviewDao.calcAvgRating(booking.getProviderId());
        userDao.updateAvgRating(booking.getProviderId(), newAvg);

        return review;
    }

    public List<Review> getByProvider(int providerId) {
        return reviewDao.findByProvider(providerId);
    }
}
