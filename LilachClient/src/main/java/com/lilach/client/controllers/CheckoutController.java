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
    @FXML private ToggleButton fastDeliveryToggle;
    @FXML private ToggleButton pickupToggle;
    
    @FXML private VBox deliveryInfoSection;
    @FXML private VBox pickupInfoSection;

    @FXML private DatePicker deliveryDatePicker;
    @FXML private ComboBox<String> deliveryHourCombo;
    @FXML private ComboBox<String> deliveryMinuteCombo;
    @FXML private TextArea deliveryAddress;
    @FXML private TextField recipientName;
    @FXML private TextField recipientPhone;
    @FXML private TextArea greetingMessage;
    @FXML private ListView<String> orderItems;
    @FXML private Label subtotalLabel;
    @FXML private Label totalLabel;

    @FXML private ComboBox<StoreDTO> pickupStoreCombo;
    @FXML private DatePicker pickupDatePicker;
    @FXML private ComboBox<String> pickupHourCombo;
    @FXML private ComboBox<String> pickupMinuteCombo;
    @FXML private TextField pickupPersonField;

    private ToggleGroup deliveryMethodGroup;
    private static final double DELIVERY_FEE = 10.00;
    private static final double FAST_DELIVERY_FEE = 20.00;
    private static final double PICKUP_FEE = 0.00;
    private double currentSubtotal = 0.0;

    
    ObservableList<CartItem> cartItems = CartService.getInstance().getCartItems();
    
    @FXML
    public void initialize() {
        // Set default delivery date (tomorrow)
        deliveryDatePicker.setValue(LocalDate.now().plusDays(1));

        setupDeliveryMethodToggle();
        initializeTimeComboBoxes();
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
        currentSubtotal = CartService.getInstance().getCartTotal();
        subtotalLabel.setText(String.format("$%.2f", currentSubtotal));
        totalLabel.setText(String.format("$%.2f", currentSubtotal + 10.00)); // Add $10 delivery fee
    }

    private void initializeTimeComboBoxes() {
        // Populate hours (09:00 to 20:00 for business hours)
        ObservableList<String> hours = FXCollections.observableArrayList();
        for (int i = 9; i <= 20; i++) {
            hours.add(String.format("%02d", i));
        }
        deliveryHourCombo.setItems(hours);
        pickupHourCombo.setItems(hours);
        
        // Populate minutes (00, 15, 30, 45)
        ObservableList<String> minutes = FXCollections.observableArrayList("00", "15", "30", "45");
        deliveryMinuteCombo.setItems(minutes);
        pickupMinuteCombo.setItems(minutes);
        
        // Set default time to 14:00
        deliveryHourCombo.setValue("14");
        deliveryMinuteCombo.setValue("00");
        pickupHourCombo.setValue("14");
        pickupMinuteCombo.setValue("00");
    }

    
    private void setupDeliveryMethodToggle() {
        deliveryMethodGroup = new ToggleGroup();
        deliveryToggle.setToggleGroup(deliveryMethodGroup);
        fastDeliveryToggle.setToggleGroup(deliveryMethodGroup);
        pickupToggle.setToggleGroup(deliveryMethodGroup);
        deliveryToggle.setSelected(true);
        
        deliveryMethodGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == deliveryToggle) {
                showDeliverySection();
                updateDeliveryFee(DELIVERY_FEE);
            } else if (newValue == fastDeliveryToggle) {
                showDeliverySection();
                updateDeliveryFee(FAST_DELIVERY_FEE);
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
    private void handleFastDeliveryToggle() {
        showDeliverySection();
        updateDeliveryFee(FAST_DELIVERY_FEE);
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
        
        // Determine delivery fee based on selected option
        double deliveryFee;
        String deliveryType;
        
        if (deliveryToggle.isSelected()) {
            deliveryFee = DELIVERY_FEE;
            deliveryType = "Delivery";
        } else if (fastDeliveryToggle.isSelected()) {
            deliveryFee = FAST_DELIVERY_FEE;
            deliveryType = "Fast Delivery";
        } else {
            deliveryFee = PICKUP_FEE;
            deliveryType = "Pickup";
        }
        
        order.setdeliveryFee(deliveryFee);
        order.setdeliveryType(deliveryType);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalPrice(CartService.getInstance().getCartTotal() + deliveryFee);
        order.setGreetingMessage(greetingMessage.getText());
        

         if (deliveryToggle.isSelected() || fastDeliveryToggle.isSelected()) {
            String timeString = deliveryHourCombo.getValue() + ":" + deliveryMinuteCombo.getValue();
            order.setDeliveryDate(deliveryDatePicker.getValue().atTime(LocalTime.parse(timeString)));
            order.setDeliveryAddress(deliveryAddress.getText());
            order.setRecipientName(recipientName.getText());
            order.setRecipientPhone(recipientPhone.getText());
        } else {
            String timeString = pickupHourCombo.getValue() + ":" + pickupMinuteCombo.getValue();
            order.setDeliveryDate(pickupDatePicker.getValue().atTime(LocalTime.parse(timeString)));
            order.setRecipientName(pickupPersonField.getText());
        }
        
        // Set order details
        
        // Convert cart items to order items
        List<OrderItemDTO> items = new ArrayList<>();
        for (CartItem orderItemDTO : CartService.getInstance().getCartItems()) {
            OrderItemDTO orderItem = new OrderItemDTO();
            orderItem.setProduct(new ProductDTO());
            orderItem.setProductId(orderItemDTO.getId());
            orderItem.setQuantity(orderItemDTO.getQuantity());
            items.add(orderItem);
        }
        
        order.setItems(items);
        
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
        
        if (deliveryHourCombo.getValue() == null || deliveryMinuteCombo.getValue() == null) {
            if (deliveryHourCombo.getValue() == null) deliveryHourCombo.getStyleClass().add("field-error");
            if (deliveryMinuteCombo.getValue() == null) deliveryMinuteCombo.getStyleClass().add("field-error");
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
        
        if (deliveryToggle.isSelected() || fastDeliveryToggle.isSelected()) {
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
        
        if (pickupHourCombo.getValue() == null || pickupMinuteCombo.getValue() == null) {
            if (pickupHourCombo.getValue() == null) pickupHourCombo.getStyleClass().add("field-error");
            if (pickupMinuteCombo.getValue() == null) pickupMinuteCombo.getStyleClass().add("field-error");
            valid = false;
        }
        
        if (pickupPersonField.getText().trim().isEmpty()) {
            pickupPersonField.getStyleClass().add("field-error");
            valid = false;
        }
        
        return valid;
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
    
    @FXML
    private void handleComplaints() {
        navigateTo("/com/lilach/client/views/complaints.fxml", "Complaints Form");
    }

    @FXML
    private void navigateToCatalog() {
        navigateToWithSize("/com/lilach/client/views/catalog.fxml", "Lilach Flower Shop Catalog", 1200, 800);
    }
    

}