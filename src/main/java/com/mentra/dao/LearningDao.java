package com.mentra.dao;

import com.mentra.model.LearningResource;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class LearningDao {

    private final SessionFactory sessionFactory;

    @Autowired
    public LearningDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    public List<LearningResource> getAllResources() {
        return getSession()
                .createQuery("FROM LearningResource ORDER BY resourceId", LearningResource.class)
                .getResultList();
    }

    public LearningResource getResourceById(Long resourceId) {
        return getSession().get(LearningResource.class, resourceId);
    }

    public List<LearningResource> getResourcesByCategory(String category) {
        return getSession()
                .createQuery("FROM LearningResource WHERE category = :category", LearningResource.class)
                .setParameter("category", category)
                .getResultList();
    }
}
