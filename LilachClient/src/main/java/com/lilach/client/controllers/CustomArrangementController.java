package com.lilach.client.controllers;

import com.lilach.client.services.CartItem;
import com.lilach.client.services.CartService;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

public class CustomArrangementController extends BaseController  {
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextArea flowerTypesField;
    @FXML private ComboBox<String> colorCombo;
    @FXML private ComboBox<String> priceCombo;
    @FXML private TextArea specialRequests;
    
    @FXML
    public void initialize() {
        // Set default selections
        typeCombo.getItems().setAll("Bouquet", "Box", "Basket", "Vase");
        colorCombo.getItems().setAll("Red", "Pink", "White", "Yellow", "Mixed");
        priceCombo.getItems().setAll("$20-$40", "$40-$60", "$60-$80", "$80+");
        typeCombo.getSelectionModel().selectFirst();
        colorCombo.getSelectionModel().selectFirst();
        priceCombo.getSelectionModel().selectFirst();
    }
    
    @FXML
    private void handleAddToCart() {
        if (validateForm()) {
            // create product from input data and add to cart
            String type = typeCombo.getValue();
            String flowerTypes = flowerTypesField.getText();
            String color = colorCombo.getValue();
            String priceRange = priceCombo.getValue();
            String requests = specialRequests.getText();
            
            // Generate unique negative ID using timestamp (ensures each custom item is unique)
            int uniqueId = -(int)(System.currentTimeMillis() % Integer.MAX_VALUE);
            
            // Determine price from price range
            double price = getPriceFromRange(priceRange);
            
            // Create cart item with all custom fields
            CartItem cartItem = new CartItem(uniqueId, "Custom " + type, price, 1, "com/lilach/client/images/logo1.png");
            cartItem.setCustomType(type);
            cartItem.setCustomColor(color);
            cartItem.setCustomPriceRange(priceRange);
            cartItem.setCustomFlowerTypes(flowerTypes);
            cartItem.setCustomSpecialRequests(requests);
            
            CartService.getInstance().addItem(cartItem);

            showSuccess("Added to Cart", "Your custom arrangement has been added to cart!");
            navigateToWithSize("/com/lilach/client/views/catalog.fxml", "Lilach Flower Shop Catalog", 1200, 800);        
        }
    }
    
    private double getPriceFromRange(String priceRange) {
        // Extract midpoint of price range for cart display
        switch(priceRange) {
            case "$20-$40": return 30.0;
            case "$40-$60": return 50.0;
            case "$60-$80": return 70.0;
            case "$80+": return 90.0;
            default: return 50.0;
        }
    }
    
    @FXML
    private void handleCancel() {
        navigateToWithSize("/com/lilach/client/views/catalog.fxml", "Lilach Flower Shop Catalog", 1200, 800);
    }
    
    private boolean validateForm() {
        if (flowerTypesField.getText().isEmpty()) {
            showError("Validation Error", "Please specify flower types");
            return false;
        }
        return true;
    }
    
    @FXML
    private void handleLogout() {
        logout();
    }
    
    @FXML
    private void handleViewCart() {
        navigateTo("/com/lilach/client/views/cart.fxml", "Shopping Cart");
    }
    
    @FXML
    private void handleViewOrders() {
        navigateTo("/com/lilach/client/views/order_history.fxml", "My Orders");
    }

    @FXML
    private void navigateToCatalog() {
        navigateToWithSize("/com/lilach/client/views/catalog.fxml", "Lilach Flower Shop Catalog", 1200, 800);
    }

    @FXML
    private void handleComplaints() {
        navigateTo("/com/lilach/client/views/complaints.fxml", "Complaints Form");
    }


}