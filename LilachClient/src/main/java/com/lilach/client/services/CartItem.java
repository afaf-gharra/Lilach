package com.lilach.client.services;

import javafx.beans.property.*;

public class CartItem {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty name;
    private final SimpleDoubleProperty price;
    private final SimpleIntegerProperty quantity;
    private final SimpleDoubleProperty total;
    private final SimpleStringProperty imageUrl;
    private final SimpleIntegerProperty productId;
    
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
}