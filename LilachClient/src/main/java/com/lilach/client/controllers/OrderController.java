package com.lilach.client.controllers;

import com.lilach.client.models.OrderDTO;
import com.lilach.client.services.ApiService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class OrderController extends BaseController {
    @FXML private DatePicker deliveryDatePicker;
    @FXML private TextField deliveryTimeField;
    @FXML private TextArea deliveryAddressArea;
    @FXML private TextField recipientNameField;
    @FXML private TextField recipientPhoneField;
    @FXML private TextArea greetingMessageArea;
    @FXML private VBox cartItemsContainer;
    @FXML private Label totalPriceLabel;
    @FXML private Button submitOrderButton;
    
    private double totalPrice = 0.0;
    
    @FXML
    public void initialize() {
        // In a real implementation, this would be populated from the cart
        populateCartItems();
        calculateTotal();
    }
    
    private void populateCartItems() {
        // Sample cart items
        cartItemsContainer.getChildren().add(new Label("• Red Roses Bouquet (x1) - $45.00"));
        cartItemsContainer.getChildren().add(new Label("• Custom Arrangement (x1) - $80.00"));
        totalPrice = 125.0;
    }
    
    private void calculateTotal() {
        totalPriceLabel.setText(String.format("Total: $%.2f", totalPrice));
    }
    
    @FXML
    private void handleSubmitOrder() {
        if (validateForm()) {
            try {
                OrderDTO order = new OrderDTO();
                order.setUserId(1); // In real app, this would come from logged-in user
                order.setDeliveryDate(combineDateAndTime());
                order.setDeliveryAddress(deliveryAddressArea.getText());
                order.setRecipientName(recipientNameField.getText());
                order.setRecipientPhone(recipientPhoneField.getText());
                order.setGreetingMessage(greetingMessageArea.getText());
                order.setTotalPrice(totalPrice);
                
                OrderDTO createdOrder = ApiService.createOrder(order);
                if (createdOrder != null) {
                    showSuccess("Order Created", "Your order has been placed successfully!");
                    // Clear form
                    clearForm();
                } else {
                    showError("Order Failed", "Failed to place your order. Please try again.");
                }
            } catch (IOException e) {
                showError("Connection Error", "Failed to connect to server: " + e.getMessage());
            }
        }
    }
    
    private LocalDateTime combineDateAndTime() {
        LocalDate date = deliveryDatePicker.getValue();
        LocalTime time = LocalTime.parse(deliveryTimeField.getText(), 
            DateTimeFormatter.ofPattern("HH:mm"));
        return LocalDateTime.of(date, time);
    }
    
    private boolean validateForm() {
        if (deliveryDatePicker.getValue() == null) {
            showError("Validation Error", "Please select a delivery date");
            return false;
        }
        if (!deliveryTimeField.getText().matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            showError("Validation Error", "Please enter a valid time in HH:mm format");
            return false;
        }
        if (deliveryAddressArea.getText().isEmpty()) {
            showError("Validation Error", "Please enter a delivery address");
            return false;
        }
        if (recipientNameField.getText().isEmpty()) {
            showError("Validation Error", "Please enter recipient name");
            return false;
        }
        if (recipientPhoneField.getText().isEmpty()) {
            showError("Validation Error", "Please enter recipient phone");
            return false;
        }
        return true;
    }
    
    private void clearForm() {
        deliveryDatePicker.setValue(null);
        deliveryTimeField.clear();
        deliveryAddressArea.clear();
        recipientNameField.clear();
        recipientPhoneField.clear();
        greetingMessageArea.clear();
        cartItemsContainer.getChildren().clear();
        totalPriceLabel.setText("Total: $0.00");
    }
    

}