package com.lilach.server.services;

import com.lilach.server.models.Order;
import com.lilach.server.models.OrderItem;
import com.lilach.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDateTime;
import java.util.List;

public class OrderService {
    public static Order createOrder(Order order) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            
            // Set order date
            order.setOrderDate(LocalDateTime.now());
            
            // Calculate total price
            double total = order.getItems().stream()
                .mapToDouble(item -> {
                    if (item.getProduct() != null) {
                        return item.getProduct().getPrice() * item.getQuantity();
                    } else {
                        // Custom item price calculation
                        return calculateCustomItemPrice(item);
                    }
                })
                .sum();
            order.setTotalPrice(total);
            
            // Save order and items
            session.persist(order);
            for (OrderItem item : order.getItems()) {
                item.setOrder(order);
                session.persist(item);
            }
            
            transaction.commit();
            return order;
        }
    }

    //updateOrderStatus
    public static Order updateOrderStatus(int orderId, String status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            
            Order order = session.get(Order.class, orderId);
            if (order != null) {
                order.setStatus(Order.OrderStatus.valueOf(status));
                session.update(order);
                transaction.commit();
                return order;
            }
            return null;
        }
    }

    //getStoreOrders
    public static List<Order> getStoreOrders(int storeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM Order WHERE store.id = :storeId ORDER BY orderDate DESC", Order.class)
                .setParameter("storeId", storeId)
                .list();
        }
    }
    
    private static double calculateCustomItemPrice(OrderItem item) {
        // Simplified custom pricing logic
        if (item.getCustomPriceRange() != null) {
            if (item.getCustomPriceRange().equals("LOW")) return 50.0;
            if (item.getCustomPriceRange().equals("MEDIUM")) return 100.0;
            if (item.getCustomPriceRange().equals("HIGH")) return 200.0;
        }
        return 80.0; // Default price
    }
    
    public static List<Order> getUserOrders(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM Order WHERE user.id = :userId ORDER BY orderDate DESC", Order.class)
                .setParameter("userId", userId)
                .list();
        }
    }
    
    public static Order cancelOrder(int orderId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            
            Order order = session.get(Order.class, orderId);
            if (order != null) {
                order.setStatus(Order.OrderStatus.CANCELLED);
                session.update(order);
                transaction.commit();
                return order;
            }
            return null;
        }
    }
    

}