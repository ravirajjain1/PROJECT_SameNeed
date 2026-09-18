package com.sameneed.thread;

import com.sameneed.dao.OfferDao;
import com.sameneed.enums.OfferStatus;
import com.sameneed.model.Offer;
import com.sameneed.service.NotificationService;

import java.util.List;

public class OfferExpiryWorker implements Runnable {

    private static final long CHECK_INTERVAL_MS = 60_000L;
    private final Object lock = new Object();
    private volatile boolean running = false;
    private Thread workerThread;

    private final OfferDao offerDao = new OfferDao();
    private final NotificationService notificationService = new NotificationService();

    public void start() {
        running = true;
        workerThread = new Thread(this, "OfferExpiryWorker-Thread");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    public void stop() {
        running = false;
        if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(CHECK_INTERVAL_MS);
                checkExpiredOffers();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private synchronized void checkExpiredOffers() {
        List<Offer> expired;
        synchronized (lock) {
            expired = offerDao.findExpiredPending();
        }
        for (Offer offer : expired) {
            try {
                offerDao.updateStatus(offer.getOfferId(), OfferStatus.EXPIRED);
                notificationService.queueForUser(offer.getProviderId(),
                        "Your offer #" + offer.getOfferId() + " has expired.");
            } catch (Exception ignored) {}
        }
    }
}
