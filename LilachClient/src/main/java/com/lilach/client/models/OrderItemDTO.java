package com.lilach.client.models;

public class OrderItemDTO {
    private int id;
    private Integer productId;
    private String customType;
    private String customPriceRange;
    private String customColor;
    private int quantity;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getCustomType() { return customType; }
    public void setCustomType(String customType) { this.customType = customType; }
    public String getCustomPriceRange() { return customPriceRange; }
    public void setCustomPriceRange(String customPriceRange) { this.customPriceRange = customPriceRange; }
    public String getCustomColor() { return customColor; }
    public void setCustomColor(String customColor) { this.customColor = customColor; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}