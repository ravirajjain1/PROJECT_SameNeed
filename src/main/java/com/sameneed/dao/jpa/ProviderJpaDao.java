package com.sameneed.dao.jpa;

import com.sameneed.config.JpaConfig;
import com.sameneed.exception.DatabaseException;
import com.sameneed.model.ProviderProfile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class ProviderJpaDao {

    private final EntityManagerFactory emf;

    public ProviderJpaDao() {
        this.emf = JpaConfig.getInstance().getEntityManagerFactory();
    }

    public List<ProviderProfile> findVerified() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<ProviderProfile> query = em.createQuery(
                    "SELECT p FROM ProviderProfile p WHERE p.verified = true ORDER BY p.avgRating DESC",
                    ProviderProfile.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<ProviderProfile> findUnverified() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<ProviderProfile> query = em.createQuery(
                    "SELECT p FROM ProviderProfile p WHERE p.verified = false",
                    ProviderProfile.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public ProviderProfile findById(int providerId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(ProviderProfile.class, providerId);
        } finally {
            em.close();
        }
    }

    public long countVerified() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(p) FROM ProviderProfile p WHERE p.verified = true",
                    Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }
}
