package com.lilach.client.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

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
    @FXML private Label fastDeliveryTimeLabel;
    @FXML private TextArea deliveryAddress;
    @FXML private TextField recipientName;
    @FXML private TextField recipientPhone;
    @FXML private TextArea greetingMessage;
    @FXML private ListView<String> orderItems;
    @FXML private Label subtotalLabel;
    @FXML private HBox discountRow;
    @FXML private Label discountLabel;
    @FXML private Label totalLabel;

    @FXML private ComboBox<StoreDTO> pickupStoreCombo;
    @FXML private DatePicker pickupDatePicker;
    @FXML private ComboBox<String> pickupHourCombo;
    @FXML private ComboBox<String> pickupMinuteCombo;
    @FXML private TextField pickupPersonField;

    private ToggleGroup deliveryMethodGroup;
    // Base fees vary by account type
    private static final double PICKUP_FEE = 0.00;
    private double currentSubtotal = 0.0;
    private double currentDiscount = 0.0;

    
    ObservableList<CartItem> cartItems = CartService.getInstance().getCartItems();
    
    @FXML
    public void initialize() {
        // Set default delivery date (tomorrow)
        deliveryDatePicker.setValue(LocalDate.now().plusDays(1));

        // hide fast delivery label by default (in case FXML exists)
        if (fastDeliveryTimeLabel != null) {
            fastDeliveryTimeLabel.setVisible(false);
            fastDeliveryTimeLabel.setManaged(false);
        }

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
    applyMemberDiscountIfEligible();
    // Initialize with regular delivery by default
    updateDeliveryFee(getDeliveryFeeForAccount(false));
    }

    private double getDeliveryFeeForAccount(boolean fast) {
        String type = (getLoggedInUser() != null && getLoggedInUser().getAccountType() != null)
            ? getLoggedInUser().getAccountType().toUpperCase()
            : "STORE";
        switch (type) {
            case "MEMBER":
                return fast ? 10.0 : 0.0;
            case "CHAIN":
                return fast ? 25.0 : 15.0;
            case "STORE":
            default:
                return fast ? 20.0 : 10.0;
        }
    }

    private void applyMemberDiscountIfEligible() {
        String type = (getLoggedInUser() != null && getLoggedInUser().getAccountType() != null)
            ? getLoggedInUser().getAccountType().toUpperCase()
            : "STORE";
        if ("MEMBER".equals(type) && currentSubtotal > 50.0) {
            currentDiscount = Math.round(currentSubtotal * 0.10 * 100.0) / 100.0; // 10% rounded to cents
            if (discountRow != null) {
                discountRow.setVisible(true);
                discountRow.setManaged(true);
            }
            if (discountLabel != null) {
                discountLabel.setText(String.format("-$%.2f", currentDiscount));
            }
        } else {
            currentDiscount = 0.0;
            if (discountRow != null) {
                discountRow.setVisible(false);
                discountRow.setManaged(false);
            }
        }
        // Recompute total with currently selected delivery type
        double fee;
        if (fastDeliveryToggle.isSelected()) fee = getDeliveryFeeForAccount(true);
        else if (deliveryToggle.isSelected()) fee = getDeliveryFeeForAccount(false);
        else fee = PICKUP_FEE;
        updateDeliveryFee(fee);
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
                updateDeliveryFee(getDeliveryFeeForAccount(false));
                // enable manual date/time selection
                enableDeliveryDateTime(true);
                if (fastDeliveryTimeLabel != null) {
                    fastDeliveryTimeLabel.setVisible(false);
                    fastDeliveryTimeLabel.setManaged(false);
                }
            } else if (newValue == fastDeliveryToggle) {
                showDeliverySection();
                updateDeliveryFee(getDeliveryFeeForAccount(true));
                // set and lock fast delivery time
                setFastDeliveryTime();
                enableDeliveryDateTime(false);
            } else if (newValue == pickupToggle) {
                showPickupSection();
                updateDeliveryFee(PICKUP_FEE);
                enableDeliveryDateTime(true);
                if (fastDeliveryTimeLabel != null) {
                    fastDeliveryTimeLabel.setVisible(false);
                    fastDeliveryTimeLabel.setManaged(false);
                }
            }
        });
    }

    // Disable/enable date and time controls
    private void enableDeliveryDateTime(boolean enable) {
        deliveryDatePicker.setDisable(!enable);
        deliveryHourCombo.setDisable(!enable);
        deliveryMinuteCombo.setDisable(!enable);
    }

    // Compute fast delivery time (3 hours from now) rounded to nearest 15 minutes,
    // update the date/time controls and show a label. Also lock the controls.
    private void setFastDeliveryTime() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fast = now.plusHours(3);
        // round minutes to nearest 15
        int minute = fast.getMinute();
        int rounded = roundToNearestQuarter(minute);
        if (rounded == 60) {
            fast = fast.plusHours(1).withMinute(0);
        } else {
            fast = fast.withMinute(rounded);
        }
        // Ensure seconds/nanos cleared
        fast = fast.withSecond(0).withNano(0);

        // Update UI
        deliveryDatePicker.setValue(fast.toLocalDate());
        deliveryHourCombo.setValue(String.format("%02d", fast.getHour()));
        deliveryMinuteCombo.setValue(String.format("%02d", fast.getMinute()));
        if (fastDeliveryTimeLabel != null) {
            fastDeliveryTimeLabel.setText("Estimated delivery: " + fast.toLocalDate() + " " + String.format("%02d:%02d", fast.getHour(), fast.getMinute()));
            fastDeliveryTimeLabel.setVisible(true);
            fastDeliveryTimeLabel.setManaged(true);
        }
    }

    private int roundToNearestQuarter(int minute) {
        // rounds to 0,15,30,45 or 60
        int mod = minute % 15;
        if (mod == 0) return minute;
        if (mod >= 8) {
            // round up
            int up = minute + (15 - mod);
            return up >= 60 ? 60 : up;
        } else {
            // round down
            return minute - mod;
        }
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
        String newText = change.getControlNewText();

        if (newText.isEmpty()) return change;

        if (newText.equals("0") || newText.equals("05")) return change;

        if (newText.matches("05\\d{0,8}")) return change;

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
        double total = currentSubtotal - currentDiscount + fee;
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
        updateDeliveryFee(getDeliveryFeeForAccount(false));
    }

    @FXML
    private void handleFastDeliveryToggle() {
        showDeliverySection();
        updateDeliveryFee(getDeliveryFeeForAccount(true));
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
            deliveryFee = getDeliveryFeeForAccount(false);
            deliveryType = "Delivery";
        } else if (fastDeliveryToggle.isSelected()) {
            deliveryFee = getDeliveryFeeForAccount(true);
            deliveryType = "Fast Delivery";
        } else {
            deliveryFee = PICKUP_FEE;
            deliveryType = "Pickup";
        }
        
        order.setdeliveryFee(deliveryFee);
        order.setdeliveryType(deliveryType);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
    // total = (subtotal - discount) + delivery
    order.setTotalPrice((CartService.getInstance().getCartTotal() - currentDiscount) + deliveryFee);
        order.setGreetingMessage(greetingMessage.getText());
        

        if (fastDeliveryToggle.isSelected()) {
            // compute fast delivery time: 3 hours from now, rounded to nearest 15 minutes
            LocalDateTime fast = LocalDateTime.now().plusHours(3).withSecond(0).withNano(0);
            int rounded = roundToNearestQuarter(fast.getMinute());
            if (rounded == 60) {
                fast = fast.plusHours(1).withMinute(0);
            } else {
                fast = fast.withMinute(rounded);
            }
            order.setDeliveryDate(fast);
            order.setDeliveryAddress(deliveryAddress.getText());
            order.setRecipientName(recipientName.getText());
            order.setRecipientPhone(recipientPhone.getText());
        } else if (deliveryToggle.isSelected()) {
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
        for (CartItem cartItem : CartService.getInstance().getCartItems()) {
            OrderItemDTO orderItem = new OrderItemDTO();
            
            // Check if this is a custom product (negative ID) or regular product
            if (cartItem.isCustomProduct()) {
                // Custom product - don't set product, populate custom fields instead
                orderItem.setProduct(null);
                orderItem.setCustomType(cartItem.getCustomType());
                orderItem.setCustomColor(cartItem.getCustomColor());
                orderItem.setCustomPriceRange(cartItem.getCustomPriceRange());
                orderItem.setCustomFlowerTypes(cartItem.getCustomFlowerTypes());
                orderItem.setCustomSpecialRequests(cartItem.getCustomSpecialRequests());
            } else {
                // Regular product - set product reference
                ProductDTO product = new ProductDTO();
                product.setId(cartItem.getId());
                orderItem.setProduct(product);
            }
            
            orderItem.setQuantity(cartItem.getQuantity());
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

        // Check that the selected pickup datetime is not in the past
        if (pickupDatePicker.getValue() != null && pickupHourCombo.getValue() != null && pickupMinuteCombo.getValue() != null) {
            try {
                LocalDate selDate = pickupDatePicker.getValue();
                LocalTime selTime = LocalTime.parse(pickupHourCombo.getValue() + ":" + pickupMinuteCombo.getValue());
                LocalDateTime selected = selDate.atTime(selTime);
                // require at least 1 hour lead time
                LocalDateTime minAllowed = LocalDateTime.now().plusHours(1);
                if (selected.isBefore(minAllowed)) {
                    // mark fields and show error
                    pickupHourCombo.getStyleClass().add("field-error");
                    pickupMinuteCombo.getStyleClass().add("field-error");
                    showError("Validation Error", "Selected pickup time must be at least 1 hour from now.");
                    valid = false;
                }
            } catch (Exception e) {
                pickupHourCombo.getStyleClass().add("field-error");
                pickupMinuteCombo.getStyleClass().add("field-error");
                showError("Validation Error", "Invalid pickup time selected.");
                valid = false;
            }
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