package com.lilach.client.controllers;

import com.lilach.client.models.OrderDTO;
import com.lilach.client.services.ApiService;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import java.io.IOException;

public class OrderHistoryController {
    @FXML private ListView<OrderDTO> ordersListView;
    @FXML private Label orderDetailsLabel;
    @FXML private Button cancelButton;
    
    private int currentUserId = 1; // Would come from logged-in user
    
    @FXML
    public void initialize() {
        loadOrders();
        
        ordersListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> showOrderDetails(newValue));
    }
    
    private void loadOrders() {
        try {
            ordersListView.getItems().setAll(ApiService.getUserOrders(currentUserId));
        } catch (IOException e) {
            orderDetailsLabel.setText("Error loading orders: " + e.getMessage());
        }
    }
    
    private void showOrderDetails(OrderDTO order) {
        if (order != null) {
            StringBuilder details = new StringBuilder();
            details.append("Order ID: ").append(order.getId()).append("\n");
            details.append("Order Date: ").append(order.getOrderDate()).append("\n");
            details.append("Delivery Date: ").append(order.getDeliveryDate()).append("\n");
            details.append("Status: ").append(order.getStatus()).append("\n");
            details.append("Total: $").append(String.format("%.2f", order.getTotalPrice())).append("\n");
            details.append("\nItems:\n");
            
            // In a real app, you would iterate through items
            details.append("• Red Roses Bouquet (x1) - $45.00\n");
            details.append("• Custom Arrangement (x1) - $80.00\n");
            
            orderDetailsLabel.setText(details.toString());
            
            // Enable cancel button if order is cancellable
            cancelButton.setDisable(!order.getStatus().equals("PENDING"));
        }
    }
    
    @FXML
    private void handleCancelOrder() {
        OrderDTO selectedOrder = ordersListView.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            try {
                OrderDTO cancelledOrder = ApiService.cancelOrder(selectedOrder.getId());
                if (cancelledOrder != null) {
                    orderDetailsLabel.setText("Order cancelled successfully!");
                    loadOrders(); // Refresh list
                } else {
                    orderDetailsLabel.setText("Failed to cancel order");
                }
            } catch (IOException e) {
                orderDetailsLabel.setText("Error cancelling order: " + e.getMessage());
            }
        }
    }
}