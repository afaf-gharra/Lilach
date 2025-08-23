package com.lilach.server.services;

import com.lilach.server.models.Store;
import com.lilach.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class StoreService {
    
    public static Store createStore(Store store) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(store);
            transaction.commit();
            return store;
        }
    }
    
    public static Store getStoreById(int storeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Store.class, storeId);
        }
    }
    
    public static List<Store> getAllStores() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Store> query = session.createQuery("FROM Store WHERE isActive = true", Store.class);
            return query.list();
        }
    }
    
    public static Store updateStore(int storeId, Store storeUpdates) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Store store = session.get(Store.class, storeId);
            
            if (store != null) {
                if (storeUpdates.getName() != null) store.setName(storeUpdates.getName());
                if (storeUpdates.getAddress() != null) store.setAddress(storeUpdates.getAddress());
                if (storeUpdates.getPhone() != null) store.setPhone(storeUpdates.getPhone());
                if (storeUpdates.getEmail() != null) store.setEmail(storeUpdates.getEmail());
                store.setActive(storeUpdates.isActive());
                
                session.update(store);
                transaction.commit();
                return store;
            }
            return null;
        }
    }
    
    public static boolean deleteStore(int storeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Store store = session.get(Store.class, storeId);
            
            if (store != null) {
                // Soft delete - set inactive instead of actual deletion
                store.setActive(false);
                session.update(store);
                transaction.commit();
                return true;
            }
            return false;
        }
    }
    
    public static Store getStoreByManager(int managerId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Store> query = session.createQuery(
                "SELECT s FROM Store s JOIN s.managers m WHERE m.id = :managerId AND s.isActive = true", 
                Store.class
            );
            query.setParameter("managerId", managerId);
            return query.uniqueResult();
        }
    }
}