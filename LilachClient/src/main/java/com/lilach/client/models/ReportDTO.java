package com.lilach.client.models;

import java.time.LocalDate;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportDTO {
    private int storeId;
    private String storeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodType; // YEARLY, QUARTERLY, MONTHLY
    
    // Financial metrics
    private double totalRevenue;
    private double totalOrders;
    private double averageOrderValue;
    private double totalCost;
    private double profit;
    
    // Order metrics
    private int totalOrdersCount;
    private int completedOrders;
    private int cancelledOrders;
    private double completionRate;
    
    // Product metrics
    private int totalProductsSold;
    private Map<String, Integer> topProducts; // Product name -> quantity
    private Map<String, Integer> categories; // Category -> count
    
    // Customer metrics
    private int newCustomers;
    private int returningCustomers;
    private double customerSatisfaction; // Average rating or similar
    
    // Complaint metrics
    private int totalComplaints;
    private int resolvedComplaints;
    private int pendingComplaints;
    private double resolutionRate;
    private double averageResolutionTime; // in days
    private java.util.List<String> pendingComplaintCustomers; // Customer names with pending complaints
    
    public ReportDTO() {}
    
    // Getters and setters
    public int getStoreId() { return storeId; } public void setStoreId(int storeId) { this.storeId = storeId; }
    public String getStoreName() { return storeName; } public void setStoreName(String storeName) { this.storeName = storeName; }
    public LocalDate getStartDate() { return startDate; } public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; } public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getPeriodType() { return periodType; } public void setPeriodType(String periodType) { this.periodType = periodType; }
    public double getTotalRevenue() { return totalRevenue; } public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
    public double getTotalOrders() { return totalOrders; } public void setTotalOrders(double totalOrders) { this.totalOrders = totalOrders; }
    public double getAverageOrderValue() { return averageOrderValue; } public void setAverageOrderValue(double averageOrderValue) { this.averageOrderValue = averageOrderValue; }
    public double getTotalCost() { return totalCost; } public void setTotalCost(double totalCost) { this.totalCost = totalCost; }
    public double getProfit() { return profit; } public void setProfit(double profit) { this.profit = profit; }
    public int getTotalOrdersCount() { return totalOrdersCount; } public void setTotalOrdersCount(int totalOrdersCount) { this.totalOrdersCount = totalOrdersCount; }
    public int getCompletedOrders() { return completedOrders; } public void setCompletedOrders(int completedOrders) { this.completedOrders = completedOrders; }
    public int getCancelledOrders() { return cancelledOrders; } public void setCancelledOrders(int cancelledOrders) { this.cancelledOrders = cancelledOrders; }
    public double getCompletionRate() { return completionRate; } public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
    public int getTotalProductsSold() { return totalProductsSold; } public void setTotalProductsSold(int totalProductsSold) { this.totalProductsSold = totalProductsSold; }
    public Map<String, Integer> getTopProducts() { return topProducts; } public void setTopProducts(Map<String, Integer> topProducts) { this.topProducts = topProducts; }
    public Map<String, Integer> getCategories() { return categories; } public void setCategories(Map<String, Integer> categories) { this.categories = categories; }
    public int getNewCustomers() { return newCustomers; } public void setNewCustomers(int newCustomers) { this.newCustomers = newCustomers; }
    public int getReturningCustomers() { return returningCustomers; } public void setReturningCustomers(int returningCustomers) { this.returningCustomers = returningCustomers; }
    public double getCustomerSatisfaction() { return customerSatisfaction; } public void setCustomerSatisfaction(double customerSatisfaction) { this.customerSatisfaction = customerSatisfaction; }
    public int getTotalComplaints() { return totalComplaints; } public void setTotalComplaints(int totalComplaints) { this.totalComplaints = totalComplaints; }
    public int getResolvedComplaints() { return resolvedComplaints; } public void setResolvedComplaints(int resolvedComplaints) { this.resolvedComplaints = resolvedComplaints; }
    public int getPendingComplaints() { return pendingComplaints; } public void setPendingComplaints(int pendingComplaints) { this.pendingComplaints = pendingComplaints; }
    public double getResolutionRate() { return resolutionRate; } public void setResolutionRate(double resolutionRate) { this.resolutionRate = resolutionRate; }
    public double getAverageResolutionTime() { return averageResolutionTime; } public void setAverageResolutionTime(double averageResolutionTime) { this.averageResolutionTime = averageResolutionTime; }
    public java.util.List<String> getPendingComplaintCustomers() { return pendingComplaintCustomers; } public void setPendingComplaintCustomers(java.util.List<String> pendingComplaintCustomers) { this.pendingComplaintCustomers = pendingComplaintCustomers; }
}