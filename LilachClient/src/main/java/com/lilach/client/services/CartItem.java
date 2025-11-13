package com.lilach.client.services;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class CartItem {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty name;
    private final SimpleDoubleProperty price;
    private final SimpleIntegerProperty quantity;
    private final SimpleDoubleProperty total;
    private final SimpleStringProperty imageUrl;
    private final SimpleIntegerProperty productId;
    
    // Custom product fields
    private String customType;
    private String customColor;
    private String customPriceRange;
    private String customFlowerTypes;
    private String customSpecialRequests;
    
    public CartItem(int id, String name, double price, int quantity, String imageUrl) {
        
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.total = new SimpleDoubleProperty(price * quantity);
        this.imageUrl = new SimpleStringProperty(imageUrl);
        this.productId = new SimpleIntegerProperty();
    }
    
    // Getters
    public int getId() { return id.get(); }
    public String getName() { return name.get(); }
    public double getPrice() { return price.get(); }
    public int getQuantity() { return quantity.get(); }
    public double getTotal() { return total.get(); }
    public String getImageUrl() { return imageUrl.get(); }
    public int getProductId() { return productId.get(); }
    
    // Property getters for table binding
    public SimpleIntegerProperty idProperty() { return id; }
    public SimpleStringProperty nameProperty() { return name; }
    public SimpleDoubleProperty priceProperty() { return price; }
    public SimpleIntegerProperty quantityProperty() { return quantity; }
    public SimpleDoubleProperty totalProperty() { return total; }
    public SimpleStringProperty imageUrlProperty() { return imageUrl; }
    public SimpleIntegerProperty productIdProperty() { return productId; }
    
    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
        this.total.set(this.price.get() * quantity);
    }
    
    // Custom product getters and setters
    public String getCustomType() { return customType; }
    public void setCustomType(String customType) { this.customType = customType; }
    
    public String getCustomColor() { return customColor; }
    public void setCustomColor(String customColor) { this.customColor = customColor; }
    
    public String getCustomPriceRange() { return customPriceRange; }
    public void setCustomPriceRange(String customPriceRange) { this.customPriceRange = customPriceRange; }
    
    public String getCustomFlowerTypes() { return customFlowerTypes; }
    public void setCustomFlowerTypes(String customFlowerTypes) { this.customFlowerTypes = customFlowerTypes; }
    
    public String getCustomSpecialRequests() { return customSpecialRequests; }
    public void setCustomSpecialRequests(String customSpecialRequests) { this.customSpecialRequests = customSpecialRequests; }
    
    public boolean isCustomProduct() {
        return this.id.get() < 0;
    }
}