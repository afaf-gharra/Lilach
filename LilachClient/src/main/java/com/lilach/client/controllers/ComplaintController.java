package com.lilach.client.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import com.lilach.client.models.ComplaintDTO;
import com.lilach.client.services.ApiService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;

public class ComplaintController extends BaseController {
    
    @FXML private ComboBox<Integer> orderCombo = new ComboBox<>();
    @FXML private ComboBox<String> complaintTypeCombo = new ComboBox<>();
    @FXML private TextArea complaintDetails;
    @FXML private ComboBox<String> resolutionCombo = new ComboBox<>();
    @FXML private CheckBox contactEmail;
    @FXML private CheckBox contactPhone;
    @FXML private Button submitButton;
    @FXML private Button backButton;

    @FXML private ComboBox<String> desiredResolutionCombo = new ComboBox<>();
    @FXML private ComboBox<String> complaintTypeComboBox = new ComboBox<>();
    
    @FXML
    public void initialize() {
        initComboBoxes();
        setupComboBoxes();
        loadUserOrders();
        desiredResolutionCombo.setItems(FXCollections.observableArrayList(
            "Refund",
            "Replacement", 
            "Credit",
            "Apology",
            "Other"
        ));
        complaintTypeComboBox.setItems(FXCollections.observableArrayList(
            "Damaged Product",
            "Late Delivery",
            "Wrong Item",
            "Poor Customer Service",
            "Other"
        ));



    }
    
    private void initComboBoxes() {
        orderCombo.setItems(FXCollections.observableArrayList());
        complaintTypeCombo.setItems(FXCollections.observableArrayList(
            "Damaged Product",
            "Late Delivery",
            "Wrong Item",
            "Poor Customer Service",
            "Other"
        ));
        resolutionCombo.setItems(FXCollections.observableArrayList(
            "Refund",
            "Replacement", 
            "Credit",
            "Apology",
            "Other"
        ));
    }

    private void setupComboBoxes() {
        // Set default selections

        complaintTypeCombo.getSelectionModel().selectFirst();
        resolutionCombo.getSelectionModel().selectFirst();
        
        // Set prompt texts
        orderCombo.setPromptText("Select your order");
        complaintTypeCombo.setPromptText("Select complaint type");
        resolutionCombo.setPromptText("What would you like us to do?");
    }
    
    private void loadUserOrders() {
        Integer userId = getLoggedInUser().getId();
        try {
            // Load user's recent orders (implement this in ApiService)
            var orders = ApiService.getUserOrders(userId);
            orderCombo.getItems().addAll(orders.stream()
                .map(order -> order.getId())
                .collect(Collectors.toList()));
            
            if (orderCombo.getItems().isEmpty()) {
                showInfo("No Orders", "You haven't placed any orders yet.");
                submitButton.setDisable(true);
            }
            
        } catch (IOException e) {
            showError("Connection Error", "Failed to load your orders: " + e.getMessage());
        }
    }
    
    private void showInfo(String string, String string2) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(string);
        alert.setHeaderText(null);
        alert.setContentText(string2);
        alert.showAndWait();
    }

    @FXML
    private void handleSubmit() {
        if (validateForm()) {
            try {
                ComplaintDTO complaint = createComplaintFromForm();
                ComplaintDTO createdComplaint = ApiService.createComplaint(complaint);
                
                if (createdComplaint != null) {
                    showSuccess("Complaint Submitted", 
                        "Your complaint has been submitted successfully. Reference #: " + createdComplaint.getId() +
                        "\nWe will contact you within 24 hours.");
                    navigateToOrderHistory();
                } else {
                    showError("Submission Failed", "Failed to submit your complaint. Please try again.");
                }
            } catch (IOException e) {
                showError("Connection Error", "Failed to submit complaint: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleBack() {
        navigateToOrderHistory();
    }
    
    private boolean validateForm() {
        if (orderCombo.getValue() == null) {
            showError("Validation Error", "Please select an order");
            return false;
        }
        if (complaintTypeCombo.getValue() == null) {
            showError("Validation Error", "Please select a complaint type");
            return false;
        }
        if (complaintDetails.getText().trim().isEmpty()) {
            showError("Validation Error", "Please describe your complaint");
            return false;
        }
        if (complaintDetails.getText().trim().length() < 10) {
            showError("Validation Error", "Please provide more details about your complaint (minimum 10 characters)");
            return false;
        }
        if (!contactEmail.isSelected() && !contactPhone.isSelected()) {
            showError("Validation Error", "Please select at least one contact method");
            return false;
        }
        return true;
    }
    
    private ComplaintDTO createComplaintFromForm() {
        ComplaintDTO complaint = new ComplaintDTO();
        complaint.setOrderId(orderCombo.getValue());
        complaint.setType(complaintTypeCombo.getValue());
        complaint.setDescription(complaintDetails.getText().trim());
        complaint.setDesiredResolution(resolutionCombo.getValue());
        complaint.setContactEmail(contactEmail.isSelected());
        complaint.setContactPhone(contactPhone.isSelected());
        complaint.setUserId(getLoggedInUser().getId());
        complaint.setStatus("OPEN");
        complaint.setCreatedAt(LocalDateTime.now());
        return complaint;
    }
    
    private void navigateToOrderHistory() {
        navigateTo("/com/lilach/client/views/order_history.fxml", "My Orders");
    }
    
    @FXML
    private void handleLogout() {
        logout();
    }

    @FXML
    private void navigateToCatalog() {
        navigateToWithSize("/com/lilach/client/views/catalog.fxml", "Lilach Flower Shop Catalog", 1200, 800);
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
        // Already on complaints page
    }
    

}