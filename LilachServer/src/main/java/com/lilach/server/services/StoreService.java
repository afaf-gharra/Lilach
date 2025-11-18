package com.lilach.server.services;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.lilach.server.models.Store;
import com.lilach.server.utils.HibernateUtil;

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
            Query<Store> query = session.createQuery("FROM Store", Store.class);
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
                // Only update isActive if explicitly provided (check if name/address/phone/email are also provided to detect full update)
                // For partial updates (like discount-only), preserve current isActive
                if (storeUpdates.getName() != null || storeUpdates.getAddress() != null || 
                    storeUpdates.getPhone() != null || storeUpdates.getEmail() != null) {
                    store.setActive(storeUpdates.isActive());
                }
                
                // Update store-wide discount only when provided (0-100)
                Integer newDiscount = storeUpdates.getStoreDiscountRaw();
                if (newDiscount != null) {
                    int clamped = Math.max(0, Math.min(100, newDiscount));
                    store.setStoreDiscount(clamped);
                }
                
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
    
}