package com.lilach.server.models;

import javax.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
    
    @Column(name = "custom_type")
    private String customType;
    
    @Column(name = "custom_price_range")
    private String customPriceRange;
    
    @Column(name = "custom_color")
    private String customColor;
    
    private int quantity;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getCustomType() { return customType; }
    public void setCustomType(String customType) { this.customType = customType; }
    public String getCustomPriceRange() { return customPriceRange; }
    public void setCustomPriceRange(String customPriceRange) { this.customPriceRange = customPriceRange; }
    public String getCustomColor() { return customColor; }
    public void setCustomColor(String customColor) { this.customColor = customColor; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}