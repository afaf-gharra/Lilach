package com.lilach.client.controllers;

import javafx.fxml.FXML;

public class AdminDashboardController extends BaseController {
    @FXML
    private void handleBackToCatalog() {
        navigateToWithSize("/com/lilach/client/views/catalog.fxml", "Lilach Flower Shop Catalog", 1200, 800);
    }
    
    @FXML
    private void handleLogout() {
        logout();
    }
}