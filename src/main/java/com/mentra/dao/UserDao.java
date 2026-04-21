package com.mentra.dao;

import com.mentra.model.Role;
import com.mentra.model.User;
import com.mentra.util.PasswordUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class UserDao {

    private final SessionFactory sessionFactory;

    @Autowired
    public UserDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private Session getCurrentSession() {
        return sessionFactory.getCurrentSession();
    }

    public User login(String email, String password) {
        try {
            User user = getCurrentSession()
                    .createQuery("FROM User u WHERE u.email = :email AND u.enabled = true", User.class)
                    .setParameter("email", email)
                    .uniqueResult();

            if (user != null && user.getPasswordHash().equals(PasswordUtil.hash(password))) {
                return user;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public void register(String matricNo, String fullName, String email, String password) {
        User user = new User(matricNo, fullName, email, PasswordUtil.hash(password));
        Role studentRole = getCurrentSession()
                .createQuery("FROM Role r WHERE r.roleName = :roleName", Role.class)
                .setParameter("roleName", "ROLE_STUDENT")
                .uniqueResult();

        if (studentRole != null) {
            user.addRole(studentRole);
        }

        getCurrentSession().save(user);
    }

    public User findById(Long userId) {
        return getCurrentSession().get(User.class, userId);
    }

    public User findByEmail(String email) {
        return getCurrentSession()
                .createQuery("FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .uniqueResult();
    }

    public User findByMatricNo(String matricNo) {
        return getCurrentSession()
                .createQuery("FROM User u WHERE u.matricNo = :matricNo", User.class)
                .setParameter("matricNo", matricNo)
                .uniqueResult();
    }

    public List<User> findAll() {
        return getCurrentSession()
                .createQuery("FROM User", User.class)
                .list();
    }

    public void update(User user) {
        getCurrentSession().update(user);
    }

    public void delete(User user) {
        getCurrentSession().delete(user);
    }

    public boolean emailExists(String email) {
        Long count = getCurrentSession()
                .createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                .setParameter("email", email)
                .uniqueResult();
        return count != null && count > 0;
    }

    public boolean matricNoExists(String matricNo) {
        Long count = getCurrentSession()
                .createQuery("SELECT COUNT(u) FROM User u WHERE u.matricNo = :matricNo", Long.class)
                .setParameter("matricNo", matricNo)
                .uniqueResult();
        return count != null && count > 0;
    }
}
