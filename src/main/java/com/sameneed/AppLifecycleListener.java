package com.sameneed;

import com.sameneed.config.DatabaseConfig;
import com.sameneed.config.JpaConfig;
import com.sameneed.thread.NotificationDispatcher;
import com.sameneed.thread.OfferExpiryWorker;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppLifecycleListener implements ServletContextListener {

    private final OfferExpiryWorker expiryWorker = new OfferExpiryWorker();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        DatabaseConfig.getInstance();
        JpaConfig.getInstance();

        NotificationDispatcher.getInstance().start();
        expiryWorker.start();

        Thread shutdownHook = new Thread(new Runnable() {
            @Override
            public void run() {
                expiryWorker.stop();
                NotificationDispatcher.getInstance().stop();
            }
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        expiryWorker.stop();
        NotificationDispatcher.getInstance().stop();
        JpaConfig.getInstance().close();
    }
}
