package com.lilach.client.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.lilach.client.models.OrderDTO;
import com.lilach.client.models.OrderItemDTO;
import com.lilach.client.models.ProductDTO;
import com.lilach.client.models.StoreDTO;
import com.lilach.client.services.ApiService;
import com.lilach.client.services.CartItem;
import com.lilach.client.services.CartService;

public class CheckoutController extends BaseController  {
    @FXML private Label welcomeLabel;
    @FXML private Label deliveryFeeLabel;
    
    @FXML private ToggleButton deliveryToggle;
    @FXML private ToggleButton pickupToggle;
    
    @FXML private VBox deliveryInfoSection;
    @FXML private VBox pickupInfoSection;

    @FXML private DatePicker deliveryDatePicker;
    @FXML private TextField deliveryTimeField;
    @FXML private TextArea deliveryAddress;
    @FXML private TextField recipientName;
    @FXML private TextField recipientPhone;
    @FXML private TextArea greetingMessage;
    @FXML private ListView<String> orderItems;
    @FXML private Label subtotalLabel;
    @FXML private Label totalLabel;

    @FXML private ComboBox<StoreDTO> pickupStoreCombo;
    @FXML private DatePicker pickupDatePicker;
    @FXML private TextField pickupTimeField;
    @FXML private TextField pickupPersonField;

    private ToggleGroup deliveryMethodGroup;
    private static final double DELIVERY_FEE = 10.00;
    private static final double PICKUP_FEE = 0.00;
    private double currentSubtotal = 0.0;

    
    ObservableList<CartItem> cartItems = CartService.getInstance().getCartItems();
    
    @FXML
    public void initialize() {
        // Set default delivery date (tomorrow)
        deliveryDatePicker.setValue(LocalDate.now().plusDays(1));

        setupDeliveryMethodToggle();
        initializeFormatters();
        loadStores();
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

    
    private void setupDeliveryMethodToggle() {
        deliveryMethodGroup = new ToggleGroup();
        deliveryToggle.setToggleGroup(deliveryMethodGroup);
        pickupToggle.setToggleGroup(deliveryMethodGroup);
        deliveryToggle.setSelected(true);
        
        deliveryMethodGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == deliveryToggle) {
                showDeliverySection();
                updateDeliveryFee(DELIVERY_FEE);
            } else if (newValue == pickupToggle) {
                showPickupSection();
                updateDeliveryFee(PICKUP_FEE);
            }
        });
    }

    private void initializeFormatters() {
        // Time formatter for HH:mm format

        
        // Phone number formatter
        setupPhoneFormatter(recipientPhone);
        
        // Date restrictions - no past dates
        deliveryDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now().plusDays(1)));
            }
        });
        
        pickupDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
    }



    private void setupPhoneFormatter(TextField phoneField) {
        phoneField.setTextFormatter(new TextFormatter<String>(change -> {
            if (change.getControlNewText().matches("\\d{0,10}")) {
                return change;
            }
            return null;
        }));
    }


    private void loadStores() {
        try {
            List<StoreDTO> allStores = ApiService.getAllStores();
            pickupStoreCombo.setItems(FXCollections.observableArrayList(allStores));
            
            // Set custom cell factory to show store names
            pickupStoreCombo.setCellFactory(param -> new ListCell<StoreDTO>() {
                @Override
                protected void updateItem(StoreDTO store, boolean empty) {
                    super.updateItem(store, empty);
                    if (empty || store == null) {
                        setText(null);
                    } else {
                        setText(store.getName() + " - " + store.getAddress());
                    }
                }
            });
            
            // Set button cell for display
            pickupStoreCombo.setButtonCell(new ListCell<StoreDTO>() {
                @Override
                protected void updateItem(StoreDTO store, boolean empty) {
                    super.updateItem(store, empty);
                    if (empty || store == null) {
                        setText("Select your preferred store");
                    } else {
                        setText(store.getName());
                    }
                }
            });
            
        } catch (IOException e) {
            showError("Connection Error", "Failed to load stores: " + e.getMessage());
        }
    }


    private void updateDeliveryFee(double fee) {
        deliveryFeeLabel.setText(String.format("$%.2f", fee));
        double total = currentSubtotal + fee;
        totalLabel.setText(String.format("$%.2f", total));
        
        if (fee == 0.0) {
            deliveryFeeLabel.getStyleClass().add("free-delivery");
        } else {
            deliveryFeeLabel.getStyleClass().remove("free-delivery");
        }
    }

    @FXML
    private void handleDeliveryToggle() {
        showDeliverySection();
        updateDeliveryFee(DELIVERY_FEE);
    }

    @FXML
    private void handlePickupToggle() {
        showPickupSection();
        updateDeliveryFee(PICKUP_FEE);
    }

    private void showDeliverySection() {
        deliveryInfoSection.setVisible(true);
        deliveryInfoSection.setManaged(true);
        pickupInfoSection.setVisible(false);
        pickupInfoSection.setManaged(false);
    }

    private void showPickupSection() {
        deliveryInfoSection.setVisible(false);
        deliveryInfoSection.setManaged(false);
        pickupInfoSection.setVisible(true);
        pickupInfoSection.setManaged(true);
    }



    private void validateField(Control field, String value) {
        if (value == null || value.trim().isEmpty()) {
            field.getStyleClass().add("field-error");
        } else {
            field.getStyleClass().remove("field-error");
        }
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
        order.setdeliveryFee(deliveryToggle.isSelected() ? DELIVERY_FEE : 0.00);
        order.setdeliveryType(deliveryToggle.isSelected() ? "Delivery" : "Pickup");
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalPrice(CartService.getInstance().getCartTotal() + (deliveryToggle.isSelected() ? DELIVERY_FEE : 0.00)); // Add delivery fee
        order.setGreetingMessage(greetingMessage.getText());
        

         if (deliveryToggle.isSelected()) {
            order.setDeliveryDate(deliveryDatePicker.getValue().atTime(java.time.LocalTime.parse(deliveryTimeField.getText())));
            order.setDeliveryAddress(deliveryAddress.getText());
            order.setRecipientName(recipientName.getText());
            order.setRecipientPhone(recipientPhone.getText());
        } else {
            order.setDeliveryDate(pickupDatePicker.getValue().atTime(java.time.LocalTime.parse(pickupTimeField.getText())));
            order.setRecipientName(pickupPersonField.getText());
        }
        
        // Set order details
        
        // Convert cart items to order items
        List<OrderItemDTO> orderItems = new ArrayList<>();
        for (CartItem orderItemDTO : CartService.getInstance().getCartItems()) {
            OrderItemDTO orderItem = new OrderItemDTO();
            orderItem.setProduct(new ProductDTO());
            orderItem.setProductId(orderItemDTO.getId());
            orderItem.setQuantity(orderItemDTO.getQuantity());
            orderItems.add(orderItem);
        }
        
        order.setItems(orderItems);
        
        return order;
    }

    // private boolean validateForm() {
    //     if (deliveryDatePicker.getValue() == null) {
    //         showError("Validation Error", "Please select delivery date");
    //         return false;
    //     }
    //     if (!deliveryTimeField.getText().matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
    //         showError("Validation Error", "Please enter valid time in HH:mm format");
    //         return false;
    //     }
    //     if (deliveryAddress.getText().isEmpty()) {
    //         showError("Validation Error", "Delivery address is required");
    //         return false;
    //     }
    //     if (recipientName.getText().isEmpty()) {
    //         showError("Validation Error", "Recipient name is required");
    //         return false;
    //     }
    //     return true;
    // }

     private boolean validateDeliveryFields() {
        boolean valid = true;
        
        if (deliveryDatePicker.getValue() == null) {
            deliveryDatePicker.getStyleClass().add("field-error");
            valid = false;
        }
        
        if (!isValidTime(deliveryTimeField.getText())) {
            deliveryTimeField.getStyleClass().add("field-error");
            valid = false;
        }
        
        if (deliveryAddress.getText().trim().isEmpty()) {
            deliveryAddress.getStyleClass().add("field-error");
            valid = false;
        }
        
        if (recipientName.getText().trim().isEmpty()) {
            recipientName.getStyleClass().add("field-error");
            valid = false;
        }
        
        if (!isValidPhone(recipientPhone.getText())) {
            recipientPhone.getStyleClass().add("field-error");
            valid = false;
        }
        
        return valid;
    }

    private boolean validateForm() {
        boolean isValid = true;
        
        if (deliveryToggle.isSelected()) {
            isValid &= validateDeliveryFields();
        } else {
            isValid &= validatePickupFields();
        }
        
        return isValid;
    }


    private boolean validatePickupFields() {
        boolean valid = true;
        
        if (pickupStoreCombo.getValue() == null) {
            pickupStoreCombo.getStyleClass().add("field-error");
            valid = false;
        }
        
        if (pickupDatePicker.getValue() == null) {
            pickupDatePicker.getStyleClass().add("field-error");
            valid = false;
        }
        
        if (!isValidTime(pickupTimeField.getText())) {
            pickupTimeField.getStyleClass().add("field-error");
            valid = false;
        }
        
        if (pickupPersonField.getText().trim().isEmpty()) {
            pickupPersonField.getStyleClass().add("field-error");
            valid = false;
        }
        
        return valid;
    }

    private boolean isValidTime(String time) {
        if (time == null || time.trim().isEmpty()) return false;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime.parse(time, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{10}");
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