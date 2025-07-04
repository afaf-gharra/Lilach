package com.lilach.server.services;

import com.lilach.server.models.Product;
import com.lilach.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

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
                product.setAvailable(updates.isAvailable());
                
                session.update(product);
                transaction.commit();
                return product;
            }
            return null;
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
}