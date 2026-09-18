package com.sameneed.dao.jpa;

import com.sameneed.config.JpaConfig;
import com.sameneed.exception.DatabaseException;
import com.sameneed.model.Service;
import com.sameneed.model.ServiceCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class ServiceJpaDao {

    private final EntityManagerFactory emf;

    public ServiceJpaDao() {
        this.emf = JpaConfig.getInstance().getEntityManagerFactory();
    }

    public List<Service> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Service> query = em.createQuery(
                    "SELECT s FROM Service s ORDER BY s.category.name ASC, s.name ASC",
                    Service.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Service> findByCategory(int categoryId) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Service> query = em.createQuery(
                    "SELECT s FROM Service s WHERE s.category.categoryId = :catId ORDER BY s.name ASC",
                    Service.class);
            query.setParameter("catId", categoryId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public Service findById(int serviceId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Service.class, serviceId);
        } finally {
            em.close();
        }
    }

    public Service insert(Service service) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(service);
            em.getTransaction().commit();
            return service;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new DatabaseException("Failed to insert service", e);
        } finally {
            em.close();
        }
    }

    public void delete(int serviceId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Service s = em.find(Service.class, serviceId);
            if (s != null) em.remove(s);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new DatabaseException("Failed to delete service", e);
        } finally {
            em.close();
        }
    }
}
