package com.lilach.server.services;

import com.lilach.server.models.Store;
import com.lilach.server.models.User;
import com.lilach.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class UserService {
    public static User authenticate(String username, String password) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                "FROM User WHERE username = :username AND password = :password AND isActive = true", 
                User.class
            );
            query.setParameter("username", username);
            query.setParameter("password", password);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean usernameExists(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                "SELECT COUNT(*) FROM User WHERE username = :username", Long.class)
                .setParameter("username", username)
                .uniqueResult();
            return count != null && count > 0;
        }
    }

// Add store assignment method to UserService
    public static boolean assignStoreToManager(int userId, int storeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            
            User user = session.get(User.class, userId);
            Store store = session.get(Store.class, storeId);
            
            if (user != null && store != null) {
                user.setStore(store);
                session.update(user);
                transaction.commit();
                return true;
            }
            return false;
        }
    }

    public static Store getStoreByManager(int managerId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User manager = session.get(User.class, managerId);
            return manager != null ? manager.getStore() : null;
        }
    }


    public static User createUser(User user) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
            return user;
        }
    }

    public static User getUserById(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, userId);
        }
    }

}