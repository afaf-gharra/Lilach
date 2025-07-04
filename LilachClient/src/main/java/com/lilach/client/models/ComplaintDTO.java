package com.lilach.client.models;

import java.time.LocalDateTime;

public class ComplaintDTO {
    private int id;
    private int orderId;
    private String description;
    private LocalDateTime createdAt;
    private String status;
    private double compensation;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getCompensation() { return compensation; }
    public void setCompensation(double compensation) { this.compensation = compensation; }
}