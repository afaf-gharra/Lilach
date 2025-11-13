package com.lilach.client.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductDTO {
    private int id;
    private String name;
    private String category;
    private String description;
    private double price;
    private String color;
    private String imageUrl;
    private boolean available;
    private int discount; // Discount percentage [0-100], default 0
    private int stock;
    private StoreDTO store;
    
    // Default constructor
    public ProductDTO() {}
    

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public int getDiscount() { return discount; }
    public void setDiscount(int discount) { this.discount = discount; }
    public int getStock() { return stock; }
    public void setStock(int int1) { this.stock = int1; }
    public StoreDTO getStore() { return store; }
    public void setStore(StoreDTO store) { this.store = store; }
    
    /**
     * Get the effective discount - the maximum of product discount and store discount
     * @return effective discount percentage (0-100)
     */
    public int getEffectiveDiscount() {
        int storeDiscount = (store != null) ? store.getStoreDiscount() : 0;
        return Math.max(discount, storeDiscount);
    }
    
    /**
     * Get the effective price after applying the best available discount
     * @return price after discount
     */
    public double getEffectivePrice() {
        int effectiveDiscount = getEffectiveDiscount();
        if (effectiveDiscount > 0) {
            return price * (100 - effectiveDiscount) / 100.0;
        }
        return price;
    }
}