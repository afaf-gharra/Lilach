package com.lilach.server.services;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.lilach.server.models.Product;
import com.lilach.server.models.Store;
import com.lilach.server.utils.HibernateUtil;

public class ProductService {
    public static List<Product> getAllProducts() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Product> query = session.createQuery(
                "FROM Product WHERE isAvailable = true", Product.class);
            return query.list();
        }
    }
    
    public static List<Product> getProductsByCategory(String category) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Product> query = session.createQuery(
                "FROM Product WHERE category = :category AND isAvailable = true", 
                Product.class
            );
            query.setParameter("category", category);
            return query.list();
        }
    }
    
    public static List<Product> searchProducts(String query) {
        String searchTerm = "%" + query.toLowerCase() + "%";
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Product> q = session.createQuery(
                "FROM Product WHERE (LOWER(name) LIKE :term OR LOWER(description) LIKE :term) " +
                "AND isAvailable = true", 
                Product.class
            );
            q.setParameter("term", searchTerm);
            return q.list();
        }
    }


    // Add to ProductService.java
    public static Product createProduct(Product product) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(product);
            transaction.commit();
            return product;
        }
    }

    public static Product updateProduct(int productId, Product updates) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Product product = session.get(Product.class, productId);
            if (product != null) {
                // Update fields
                if (updates.getName() != null) product.setName(updates.getName());
                if (updates.getCategory() != null) product.setCategory(updates.getCategory());
                if (updates.getDescription() != null) product.setDescription(updates.getDescription());
                if (updates.getPrice() > 0) product.setPrice(updates.getPrice());
                if (updates.getColor() != null) product.setColor(updates.getColor());
                if (updates.getImageUrl() != null) product.setImageUrl(updates.getImageUrl());
                if (updates.getStock() >= 0) product.setStock(updates.getStock());
                product.setAvailable(updates.isAvailable());
                // Update discount field
                product.setDiscount(updates.getDiscount());
                
                session.update(product);
                transaction.commit();
                return product;
            }
            return null;
        }
    }
    // Add store-specific methods to ProductService
    public static List<Product> getProductsByStore(int storeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Product> query = session.createQuery(
                "FROM Product WHERE store.id = :storeId AND isAvailable = true", 
                Product.class
            );
            query.setParameter("storeId", storeId);
            return query.list();
        }
    }

    public static Product createProductForStore(Product product, int storeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Store store = session.get(Store.class, storeId);
            if (store == null) {
                throw new IllegalArgumentException("Store not found with ID: " + storeId);
            }
            
            product.setStore(store);
            
            Transaction transaction = session.beginTransaction();
            session.persist(product);
            transaction.commit();
            return product;
        }
    }

    public static boolean updateProductStock(int productId, int newStock) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Product product = session.get(Product.class, productId);
            
            if (product != null) {
                product.setStock(newStock);
                session.update(product);
                transaction.commit();
                return true;
            }
            return false;
        }
    }

    public static boolean deleteProduct(int productId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Product product = session.get(Product.class, productId);
            if (product != null) {
                session.delete(product);
                transaction.commit();
                return true;
            }
            return false;
        }
    }

    public static Product getProductById(int productId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Product.class, productId);
        }
    }
}