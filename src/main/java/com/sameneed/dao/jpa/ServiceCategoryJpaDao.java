package com.sameneed.dao.jpa;

import com.sameneed.config.JpaConfig;
import com.sameneed.exception.DatabaseException;
import com.sameneed.model.ServiceCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class ServiceCategoryJpaDao {

    private final EntityManagerFactory emf;

    public ServiceCategoryJpaDao() {
        this.emf = JpaConfig.getInstance().getEntityManagerFactory();
    }

    public List<ServiceCategory> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<ServiceCategory> query = em.createQuery(
                    "SELECT c FROM ServiceCategory c ORDER BY c.name ASC",
                    ServiceCategory.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public ServiceCategory findById(int categoryId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(ServiceCategory.class, categoryId);
        } finally {
            em.close();
        }
    }

    public ServiceCategory insert(ServiceCategory category) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(category);
            em.getTransaction().commit();
            return category;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new DatabaseException("Failed to insert service category", e);
        } finally {
            em.close();
        }
    }

    public void delete(int categoryId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            ServiceCategory cat = em.find(ServiceCategory.class, categoryId);
            if (cat != null) em.remove(cat);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new DatabaseException("Failed to delete service category", e);
        } finally {
            em.close();
        }
    }
}
