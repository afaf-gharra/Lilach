package com.lilach.client.controllers;

import javafx.fxml.FXML;

public class StoreManagerController extends BaseController {
    @FXML
    private void handleBackToCatalog() {
        navigateToWithSize("/com/lilach/client/views/catalog.fxml", "Lilach Flower Shop Catalog", 1200, 800);
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

}