package com.lilach.server.models;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "orders")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "order_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime orderDate;
    
    @Column(name = "delivery_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deliveryDate;
    
    @Column(name = "delivery_address")
    private String deliveryAddress;
    
    @Column(name = "recipient_name")
    private String recipientName;
    
    @Column(name = "recipient_phone")
    private String recipientPhone;
    
    @Column(name = "greeting_message")
    private String greetingMessage;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @Column(name = "total_price")
    private double totalPrice;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items;
    
    public enum OrderStatus {
        PENDING, DELIVERED, CANCELLED
    }

    // Default constructor for JSON deserialization
    public Order() {}

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    
    public LocalDateTime getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(LocalDateTime deliveryDate) { this.deliveryDate = deliveryDate; }
    
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    
    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }
    
    public String getRecipientPhone() { return recipientPhone; }
    public void setRecipientPhone(String recipientPhone) { this.recipientPhone = recipientPhone; }
    
    public String getGreetingMessage() { return greetingMessage; }
    public void setGreetingMessage(String greetingMessage) { this.greetingMessage = greetingMessage; }
    
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}