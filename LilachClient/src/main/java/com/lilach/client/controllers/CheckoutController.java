package com.lilach.client.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.lilach.client.models.OrderDTO;
import com.lilach.client.models.OrderItemDTO;
import com.lilach.client.models.ProductDTO;
import com.lilach.client.services.ApiService;
import com.lilach.client.services.CartItem;
import com.lilach.client.services.CartService;

public class CheckoutController extends BaseController  {
    @FXML private DatePicker deliveryDatePicker;
    @FXML private TextField deliveryTimeField;
    @FXML private TextArea deliveryAddress;
    @FXML private TextField recipientName;
    @FXML private TextField recipientPhone;
    @FXML private TextArea greetingMessage;
    @FXML private ListView<String> orderItems;
    @FXML private Label subtotalLabel;
    @FXML private Label totalLabel;
    
    ObservableList<CartItem> cartItems = CartService.getInstance().getCartItems();
    
    @FXML
    public void initialize() {
        // Set default delivery date (tomorrow)
        deliveryDatePicker.setValue(LocalDate.now().plusDays(1));


        // get cart items
       
        ObservableList<String> items = FXCollections.observableArrayList(

            cartItems.stream()
                     .map(item -> item.getName() + " x" + item.getQuantity() + " - $" + String.format("%.2f", item.getTotal()))
                     .toList()
            
        );
        orderItems.setItems(items);
        
        // Set prices
        subtotalLabel.setText(String.format("$%.2f", CartService.getInstance().getCartTotal()));
        totalLabel.setText(String.format("$%.2f", CartService.getInstance().getCartTotal() + 10.00)); // Add $10 delivery fee
    }
    
    @FXML
    private void handlePlaceOrder() {
        if (validateForm()) {
            try {
                OrderDTO order = createOrderFromForm();
                OrderDTO createdOrder = ApiService.createOrder(order, getLoggedInUser().getId());
                
                if (createdOrder != null) {
                    // Clear cart after successful order
                    CartService.getInstance().clearCart();
                    showSuccess("Order Placed", "Your order has been placed successfully! Order ID: " + createdOrder.getId());
                    navigateToWithSize("/com/lilach/client/views/catalog.fxml", "Lilach Flower Shop Catalog", 1200, 800);
                } else {
                    showError("Order Failed", "Failed to place your order. Please try again.");
                }
            } catch (IOException e) {
                System.err.println("Order placement error: " + e.getMessage());
                showError("Connection Error", "Failed to connect to server: " + e.getMessage());
            }
        }
    }
    
    private OrderDTO createOrderFromForm() {
        OrderDTO order = new OrderDTO();
        
        // Set user ID (would come from logged-in user)
        order.setUserId(loggedInUser.getId());
        

        // Set delivery information
        order.setDeliveryDate(deliveryDatePicker.getValue().atTime(java.time.LocalTime.parse(deliveryTimeField.getText())));
        order.setDeliveryAddress(deliveryAddress.getText());
        order.setRecipientName(recipientName.getText());
        order.setRecipientPhone(recipientPhone.getText());
        order.setGreetingMessage(greetingMessage.getText());
        
        // Set order details
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalPrice(CartService.getInstance().getCartTotal() + 10.00); // Add delivery fee
        
        // Convert cart items to order items
        List<OrderItemDTO> orderItems = new ArrayList<>();
        for (CartItem orderItemDTO : CartService.getInstance().getCartItems()) {
            OrderItemDTO orderItem = new OrderItemDTO();
            orderItem.setProduct(orderItem.getProduct());
            orderItem.setQuantity(orderItemDTO.getQuantity());
            orderItems.add(orderItem);
        }
        
        order.setItems(orderItems);
        
        return order;
    }

    private boolean validateForm() {
        if (deliveryDatePicker.getValue() == null) {
            showError("Validation Error", "Please select delivery date");
            return false;
        }
        if (!deliveryTimeField.getText().matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            showError("Validation Error", "Please enter valid time in HH:mm format");
            return false;
        }
        if (deliveryAddress.getText().isEmpty()) {
            showError("Validation Error", "Delivery address is required");
            return false;
        }
        if (recipientName.getText().isEmpty()) {
            showError("Validation Error", "Recipient name is required");
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

    

}