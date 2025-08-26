package com.lilach.server.models;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "store_id")
    private Store store;
    
    private String type;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "desired_resolution")
    private String desiredResolution;
    
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;
    
    private Double compensation;
    
    @Enumerated(EnumType.STRING)
    private ComplaintStatus status;
    
    @Column(name = "contact_email")
    private boolean contactEmail;
    
    @Column(name = "contact_phone")
    private boolean contactPhone;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
    
    public enum ComplaintStatus {
        OPEN, IN_PROGRESS, RESOLVED, CLOSED
    }
    
    // Constructors
    public Complaint() {
        this.createdAt = LocalDateTime.now();
        this.status = ComplaintStatus.OPEN;
    }
    
    public Complaint(Order order, User user, String type, String description) {
        this();
        this.order = order;
        this.user = user;
        this.type = type;
        this.description = description;
        this.store = order.getStore();
    }
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { 
        this.order = order; 
        if (order != null && order.getStore() != null) {
            this.store = order.getStore();
        }
    }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Store getStore() { return store; }
    public void setStore(Store store) { this.store = store; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getDesiredResolution() { return desiredResolution; }
    public void setDesiredResolution(String desiredResolution) { this.desiredResolution = desiredResolution; }
    
    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
    
    public Double getCompensation() { return compensation; }
    public void setCompensation(Double compensation) { this.compensation = compensation; }
    
    public ComplaintStatus getStatus() { return status; }
    public void setStatus(ComplaintStatus status) { 
        this.status = status;
        if (status == ComplaintStatus.RESOLVED || status == ComplaintStatus.CLOSED) {
            this.resolvedAt = LocalDateTime.now();
        }
    }
    
    public boolean isContactEmail() { return contactEmail; }
    public void setContactEmail(boolean contactEmail) { this.contactEmail = contactEmail; }
    
    public boolean isContactPhone() { return contactPhone; }
    public void setContactPhone(boolean contactPhone) { this.contactPhone = contactPhone; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}