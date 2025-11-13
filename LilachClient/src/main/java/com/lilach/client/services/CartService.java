package com.lilach.client.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CartService {
    private static CartService instance;
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private double cartTotal = 0.0;
    
    private CartService() {}
    
    public static CartService getInstance() {
        if (instance == null) {
            instance = new CartService();
        }
        return instance;
    }
    
    public ObservableList<CartItem> getCartItems() {
        return cartItems;
    }
    
    public void addItem(CartItem item) {
        // For custom products (negative IDs), always add as new item
        // For regular products, check if item already exists and merge quantities
        if (item.getId() > 0) {
            for (CartItem cartItem : cartItems) {
                if (cartItem.getId() == item.getId()) {
                    cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
                    updateTotal();
                    return;
                }
            }
        }
        
        // Add new item (either custom product or first instance of regular product)
        cartItems.add(item);
        updateTotal();
    }
    
    public void removeItem(int itemId) {
        cartItems.removeIf(item -> item.getId() == itemId);
        updateTotal();
    }
    
    public void updateQuantity(int itemId, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getId() == itemId) {
                item.setQuantity(quantity);
                break;
            }
        }
        updateTotal();
    }
    
    public void clearCart() {
        cartItems.clear();
        cartTotal = 0.0;
    }
    
    public double getCartTotal() {
        return cartTotal;
    }
    
    private void updateTotal() {
        cartTotal = cartItems.stream()
                .mapToDouble(CartItem::getTotal)
                .sum();
    }
}