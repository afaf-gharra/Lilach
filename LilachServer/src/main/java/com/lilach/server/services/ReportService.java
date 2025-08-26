package com.lilach.server.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lilach.server.models.Report;
import com.lilach.server.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {
    
    private static final ObjectMapper mapper = new ObjectMapper();
    public static Report generateYearlyReport(int storeId, int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        
        Report report = new Report();
        report.setStoreId(storeId);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setPeriodType("YEARLY");
        
        // Get basic metrics
        calculateFinancialMetrics(report, storeId, startDate, endDate);
        calculateOrderMetrics(report, storeId, startDate, endDate);
        calculateProductMetrics(report, storeId, startDate, endDate);
        calculateComplaintMetrics(report, storeId, startDate, endDate);
        
        return report;
    }
    
    public static Report generateQuarterlyReport(int storeId, int year, int quarter) {
        mapper.registerModule(new JavaTimeModule());
        LocalDate startDate = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
        LocalDate endDate = startDate.plusMonths(3).minusDays(1);
        Report report = new Report();
        report.setStoreId(storeId);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setPeriodType("QUARTERLY");
        
        // Get basic metrics
        calculateFinancialMetrics(report, storeId, startDate, endDate);
        calculateOrderMetrics(report, storeId, startDate, endDate);
        calculateProductMetrics(report, storeId, startDate, endDate);
        calculateComplaintMetrics(report, storeId, startDate, endDate);
        
        return report;
    }
    
    private static void calculateFinancialMetrics(Report report, int storeId, LocalDate startDate, LocalDate endDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Total revenue
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
            Query<Double> revenueQuery = session.createQuery(
                "SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o " +
                "WHERE o.store.id = :storeId AND o.orderDate BETWEEN :startDate AND :endDate AND o.status = 'DELIVERED'", 
                Double.class
            );
            mapper.registerModule(new JavaTimeModule());
            revenueQuery.setParameter("storeId", storeId);
            revenueQuery.setParameter("startDate", startDateTime);
            revenueQuery.setParameter("endDate", endDateTime);
            Double totalRevenue = revenueQuery.uniqueResult();
            report.setTotalRevenue(totalRevenue != null ? totalRevenue : 0.0);
            
            // Total orders count for average calculation
            Query<Long> ordersCountQuery = session.createQuery(
                "SELECT COUNT(o) FROM Order o " +
                "WHERE o.store.id = :storeId AND o.orderDate BETWEEN :startDate AND :endDate AND o.status = 'DELIVERED'", 
                Long.class
            );
            ordersCountQuery.setParameter("storeId", storeId);
            ordersCountQuery.setParameter("startDate", startDateTime);
            ordersCountQuery.setParameter("endDate", endDateTime);
            Long ordersCount = ordersCountQuery.uniqueResult();
            
            // Average order value
            if (ordersCount != null && ordersCount > 0) {
                report.setAverageOrderValue(totalRevenue / ordersCount);
            }
            
            // Simplified cost calculation (assuming 60% cost of goods)
            report.setTotalCost(totalRevenue * 0.6);
            report.setProfit(totalRevenue - report.getTotalCost());
        }
    }
    
    private static void calculateOrderMetrics(Report report, int storeId, LocalDate startDate, LocalDate endDate) {
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Total orders
            Query<Long> totalOrdersQuery = session.createQuery(
                "SELECT COUNT(o) FROM Order o " +
                "WHERE o.store.id = :storeId AND o.orderDate BETWEEN :startDate AND :endDate", 
                Long.class
            );
            totalOrdersQuery.setParameter("storeId", storeId);
            totalOrdersQuery.setParameter("startDate", startDateTime);
            totalOrdersQuery.setParameter("endDate", endDateTime);
            report.setTotalOrdersCount(totalOrdersQuery.uniqueResult().intValue());
            
            // Completed orders
            Query<Long> completedOrdersQuery = session.createQuery(
                "SELECT COUNT(o) FROM Order o " +
                "WHERE o.store.id = :storeId AND o.orderDate BETWEEN :startDate AND :endDate AND o.status = 'DELIVERED'", 
                Long.class
            );
            completedOrdersQuery.setParameter("storeId", storeId);
            completedOrdersQuery.setParameter("startDate", startDateTime);
            completedOrdersQuery.setParameter("endDate", endDateTime);
            report.setCompletedOrders(completedOrdersQuery.uniqueResult().intValue());
            
            // Cancelled orders
            Query<Long> cancelledOrdersQuery = session.createQuery(
                "SELECT COUNT(o) FROM Order o " +
                "WHERE o.store.id = :storeId AND o.orderDate BETWEEN :startDate AND :endDate AND o.status = 'CANCELLED'", 
                Long.class
            );
            cancelledOrdersQuery.setParameter("storeId", storeId);
            cancelledOrdersQuery.setParameter("startDate", startDateTime);
            cancelledOrdersQuery.setParameter("endDate", endDateTime);
            report.setCancelledOrders(cancelledOrdersQuery.uniqueResult().intValue());
            
            // Completion rate
            if (report.getTotalOrdersCount() > 0) {
                report.setCompletionRate((double) report.getCompletedOrders() / report.getTotalOrdersCount() * 100);
            }
        }
    }
    
    private static void calculateProductMetrics(Report report, int storeId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Total products sold
            Query<Long> productsSoldQuery = session.createQuery(
                "SELECT COALESCE(SUM(oi.quantity), 0) FROM Order o " +
                "JOIN o.items oi WHERE o.store.id = :storeId AND o.orderDate BETWEEN :startDate AND :endDate AND o.status = 'DELIVERED'", 
                Long.class
            );
            productsSoldQuery.setParameter("storeId", storeId);
            productsSoldQuery.setParameter("startDate", startDateTime);
            productsSoldQuery.setParameter("endDate", endDateTime);
            report.setTotalProductsSold(productsSoldQuery.uniqueResult().intValue());
            
            // Top products (simplified)
            Query<Object[]> topProductsQuery = session.createQuery(
                "SELECT p.name, SUM(oi.quantity) FROM Order o " +
                "JOIN o.items oi JOIN oi.product p " +
                "WHERE o.store.id = :storeId AND o.orderDate BETWEEN :startDate AND :endDate AND o.status = 'DELIVERED' " +
                "GROUP BY p.name ORDER BY SUM(oi.quantity) DESC", 
                Object[].class
            );
            topProductsQuery.setParameter("storeId", storeId);
            topProductsQuery.setParameter("startDate", startDateTime);
            topProductsQuery.setParameter("endDate", endDateTime);
            topProductsQuery.setMaxResults(5);
            
            Map<String, Integer> topProducts = new HashMap<>();
            List<Object[]> results = topProductsQuery.list();
            for (Object[] result : results) {
                topProducts.put((String) result[0], ((Long) result[1]).intValue());
            }
            report.setTopProducts(topProducts);
        }
    }
    
    private static void calculateComplaintMetrics(Report report, int storeId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Total complaints
            Query<Long> totalComplaintsQuery = session.createQuery(
                "SELECT COUNT(c) FROM Complaint c " +
                "WHERE c.store.id = :storeId AND c.createdAt BETWEEN :startDate AND :endDate", 
                Long.class
            );
            totalComplaintsQuery.setParameter("storeId", storeId);
            totalComplaintsQuery.setParameter("startDate", startDateTime);
            totalComplaintsQuery.setParameter("endDate", endDateTime);
            report.setTotalComplaints(totalComplaintsQuery.uniqueResult().intValue());
            
            // Resolved complaints
            Query<Long> resolvedComplaintsQuery = session.createQuery(
                "SELECT COUNT(c) FROM Complaint c " +
                "WHERE c.store.id = :storeId AND c.createdAt BETWEEN :startDate AND :endDate AND c.status IN ('RESOLVED', 'CLOSED')", 
                Long.class
            );
            resolvedComplaintsQuery.setParameter("storeId", storeId);
            resolvedComplaintsQuery.setParameter("startDate", startDateTime);
            resolvedComplaintsQuery.setParameter("endDate", endDateTime);
            report.setResolvedComplaints(resolvedComplaintsQuery.uniqueResult().intValue());
            
            // Resolution rate
            if (report.getTotalComplaints() > 0) {
                report.setResolutionRate((double) report.getResolvedComplaints() / report.getTotalComplaints() * 100);
            }
        }
    }
    
    public static List<Integer> getAvailableYears(int storeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Integer> yearsQuery = session.createQuery(
                "SELECT DISTINCT YEAR(o.orderDate) FROM Order o " +
                "WHERE o.store.id = :storeId ORDER BY YEAR(o.orderDate) DESC", 
                Integer.class
            );
            yearsQuery.setParameter("storeId", storeId);
            return yearsQuery.list();
        }
    }
}