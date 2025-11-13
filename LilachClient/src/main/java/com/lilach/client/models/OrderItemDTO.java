package com.lilach.client.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderItemDTO {
    private int id;
    private ProductDTO product;  // Change from productId to ProductDTO
    private String customType;
    private String customPriceRange;
    private String customColor;
    private String customFlowerTypes;
    private String customSpecialRequests;
    private int quantity;

    // Default constructor
    public OrderItemDTO() {}

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public ProductDTO getProduct() { return product; }
    public void setProduct(ProductDTO product) { this.product = product; }
    
    public String getCustomType() { return customType; }
    public void setCustomType(String customType) { this.customType = customType; }
    
    public String getCustomPriceRange() { return customPriceRange; }
    public void setCustomPriceRange(String customPriceRange) { this.customPriceRange = customPriceRange; }
    
    public String getCustomColor() { return customColor; }
    public void setCustomColor(String customColor) { this.customColor = customColor; }
    
    public String getCustomFlowerTypes() { return customFlowerTypes; }
    public void setCustomFlowerTypes(String customFlowerTypes) { this.customFlowerTypes = customFlowerTypes; }
    
    public String getCustomSpecialRequests() { return customSpecialRequests; }
    public void setCustomSpecialRequests(String customSpecialRequests) { this.customSpecialRequests = customSpecialRequests; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public void setProductId(int id2) {
        if (product != null) {
            product.setId(id2);
        }
    }
}