package com.lilach.server.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.lilach.server.models.Order;
import com.lilach.server.models.Refund;
import com.lilach.server.utils.HibernateUtil;

public class RefundService {
    
    public static Refund calculateAndCreateRefund(int orderId, String cancellationReason) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            
            // Get the order
            Order order = session.get(Order.class, orderId);
            if (order == null) {
                throw new IllegalArgumentException("Order not found with ID: " + orderId);
            }
            
            // Calculate refund based on cancellation time
            RefundCalculationResult calculation = calculateRefund(order);
            
            // Create refund record
            Refund refund = new Refund(order, calculation.getRefundAmount(), calculation.getRefundPercentage());
            refund.setCancellationReason(cancellationReason);
            
            // Update order status
            order.setStatus(Order.OrderStatus.CANCELLED);
            order.setCancelledAt(LocalDateTime.now());
            
            session.persist(refund);
            session.update(order);
            transaction.commit();
            
            return refund;
        }
        catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error calculating refund: " + e.getMessage(), e);
        }
    }
    
    private static RefundCalculationResult calculateRefund(Order order) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deliveryTime = order.getDeliveryDate();
        
        if (deliveryTime == null) {
            throw new IllegalArgumentException("Order delivery date is not set");
        }
        
        long hoursUntilDelivery = Duration.between(now, deliveryTime).toHours();
        double orderTotal = order.getTotalPrice();
        
        int refundPercentage;
        
        if (hoursUntilDelivery <= 1) {
            refundPercentage = 0; // 0% refund if less than 1 hour
        } else if (hoursUntilDelivery <= 3) {
            refundPercentage = 50; // 50% refund if 1-3 hours
        } else {
            refundPercentage = 100; // 100% refund if more than 3 hours
        }
        
        double refundAmount = (orderTotal * refundPercentage) / 100.0;
        
        return new RefundCalculationResult(refundAmount, refundPercentage, hoursUntilDelivery);
    }
    
    public static Refund getRefundByOrderId(int orderId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM Refund WHERE order.id = :orderId", Refund.class)
                .setParameter("orderId", orderId)
                .uniqueResult();
        }
    }
    
    public static List<Refund> getRefundsByUser(int userId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "FROM Refund r WHERE r.order.user.id = :userId ORDER BY r.cancelledAt DESC", Refund.class)
                .setParameter("userId", userId)
                .list();
        }
    }
    
    // Helper class for refund calculation result
    private static class RefundCalculationResult {
        private final double refundAmount;
        private final int refundPercentage;
        private final long hoursUntilDelivery;
        
        public RefundCalculationResult(double refundAmount, int refundPercentage, long hoursUntilDelivery) {
            this.refundAmount = refundAmount;
            this.refundPercentage = refundPercentage;
            this.hoursUntilDelivery = hoursUntilDelivery;
        }
        
        public double getRefundAmount() { return refundAmount; }
        public int getRefundPercentage() { return refundPercentage; }
        public long getHoursUntilDelivery() { return hoursUntilDelivery; }
    }
}