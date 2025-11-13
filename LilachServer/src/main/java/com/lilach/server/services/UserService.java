package com.lilach.server.services;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.lilach.server.models.Store;
import com.lilach.server.models.User;
import com.lilach.server.utils.HibernateUtil;

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

    // Add these methods to UserService
    public static List<User> getAllUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            return query.list();
        }
    }

    public static User getUserById(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, userId);
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

    public static User updateUser(int userId, User userUpdates) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            User user = session.get(User.class, userId);
            
            if (user != null) {
                if (userUpdates.getUsername() != null) user.setUsername(userUpdates.getUsername());
                if (userUpdates.getFullName() != null) user.setFullName(userUpdates.getFullName());
                if (userUpdates.getEmail() != null) user.setEmail(userUpdates.getEmail());
                if (userUpdates.getPhone() != null) user.setPhone(userUpdates.getPhone());
                if (userUpdates.getRole() != null) user.setRole(userUpdates.getRole());
                if (userUpdates.getAccountType() != null) user.setAccountType(userUpdates.getAccountType());
                if (userUpdates.getStore() != null) user.setStore(userUpdates.getStore());
                if (userUpdates.getStoreId() != null) user.setStoreId(userUpdates.getStoreId());
                if (userUpdates.getCreditCard() != null) user.setCreditCard(userUpdates.getCreditCard());
                if (userUpdates.getMembershipExpiry() != null) user.setMembershipExpiry(userUpdates.getMembershipExpiry());
                user.setActive(userUpdates.isActive());
                
                session.update(user);
                transaction.commit();
                return user;
            }
            return null;
        }
    }

    public static boolean deleteUser(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            User user = session.get(User.class, userId);
            
            if (user != null) {
                // Soft delete - set inactive
                user.setActive(false);
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

    /**
     * Reset the isOnline flag for all users to false.
     * Intended to be called on server startup to clear any stale online states.
     */
    public static int resetAllOnlineFlags() {
        Transaction transaction = null;
        int updated = 0;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            // HQL bulk update to set isOnline = false for all users
            updated = session.createQuery("UPDATE User SET isOnline = false").executeUpdate();
            transaction.commit();
            return updated;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return updated;
        }
    }

    /**
     * Set a single user's isOnline flag.
     * @param userId the user id
     * @param online desired online state
     * @return true if update succeeded
     */
    public static boolean setUserOnline(int userId, boolean online) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, userId);
            if (user == null) {
                if (transaction != null) transaction.rollback();
                return false;
            }
            user.setOnline(online);
            session.update(user);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }
}