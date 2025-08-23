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
        // Check if item already exists in cart
        for (CartItem cartItem : cartItems) {
            if (cartItem.getId() == item.getId()) {
                cartItem.setQuantity(cartItem.getQuantity() + item.getQuantity());
                updateTotal();
                return;
            }
        }
        
        // Add new item
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