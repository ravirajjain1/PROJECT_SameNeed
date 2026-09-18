package com.sameneed.config;

import com.sameneed.exception.DatabaseException;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaConfig {

    private static JpaConfig instance;
    private final EntityManagerFactory emf;

    private JpaConfig() {
        try {
            emf = Persistence.createEntityManagerFactory("sameneedPU");
        } catch (Exception e) {
            throw new DatabaseException("Failed to initialize JPA EntityManagerFactory", e);
        }
    }

    public static synchronized JpaConfig getInstance() {
        if (instance == null) {
            instance = new JpaConfig();
        }
        return instance;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
