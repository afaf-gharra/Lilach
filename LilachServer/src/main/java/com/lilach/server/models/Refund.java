package com.lilach.server.models;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "refunds")
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "refund_amount")
    private double refundAmount;
    
    @Column(name = "refund_percentage")
    private int refundPercentage;
    
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status")
    private RefundStatus refundStatus;
    
    public enum RefundStatus {
        PENDING, PROCESSED, FAILED
    }
    
    // Constructors
    public Refund() {
        this.cancelledAt = LocalDateTime.now();
        this.refundStatus = RefundStatus.PENDING;
    }
    
    public Refund(Order order, double refundAmount, int refundPercentage) {
        this();
        this.order = order;
        this.refundAmount = refundAmount;
        this.refundPercentage = refundPercentage;
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    
    public double getRefundAmount() { return refundAmount; }
    public void setRefundAmount(double refundAmount) { this.refundAmount = refundAmount; }
    
    public int getRefundPercentage() { return refundPercentage; }
    public void setRefundPercentage(int refundPercentage) { this.refundPercentage = refundPercentage; }
    
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    
    public RefundStatus getRefundStatus() { return refundStatus; }
    public void setRefundStatus(RefundStatus refundStatus) { this.refundStatus = refundStatus; }
}