package com.lilach.server.models;

public class CartItemDTO {
    private int userId;
    private int productId;
    private String productName;
    private double price;
    private int quantity;
    private String imageUrl;
    
    // Constructors, getters, and setters
    public CartItemDTO() {}
    
    public CartItemDTO(int userId, int productId, String productName, double price, int quantity, String imageUrl) {
        this.userId = userId;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }
    
    // Getters and setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public double getTotal() {
        return price * quantity;
    }
}