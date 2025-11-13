package com.lilach.client.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.lilach.client.models.UserDTO;
import com.lilach.client.services.ApiService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ProfileController extends BaseController {
    @FXML private Label accountTypeLabel;
    @FXML private Label membershipStatusLabel;
    @FXML private Label membershipDaysLeftLabel;
    @FXML private VBox benefitsBox;
    @FXML private Button subscribeButton;
    @FXML private TextField cardNumberField;
    @FXML private ComboBox<String> monthCombo;
    @FXML private ComboBox<String> yearCombo;
    @FXML private TextField cvvField;
    @FXML private Button saveSubscriptionButton;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        UserDTO user = getLoggedInUser();
        if (user == null) return; // navigation already handled in BaseController
        setupCardValidation();
        populateExpiryCombos();
        renderState(user);
    }

    private void renderState(UserDTO user) {
        String type = user.getAccountType() != null ? user.getAccountType().toUpperCase() : "STORE";
        accountTypeLabel.setText(type);
        saveSubscriptionButton.setVisible(false);
        saveSubscriptionButton.setManaged(false);
        subscribeButton.setVisible(false);
        subscribeButton.setManaged(false);
        if ("MEMBER".equals(type)) {
            membershipStatusLabel.setText("Active Membership");
            LocalDate expiry = parseExpiry(user.getMembershipExpiry());
            if (expiry != null) {
                long days = ChronoUnit.DAYS.between(LocalDate.now(), expiry);
                membershipDaysLeftLabel.setText(days >= 0 ? days + " days left" : "Expired");
            } else {
                membershipDaysLeftLabel.setText("Expiry not set");
            }
            benefitsBox.setVisible(true);
            benefitsBox.setManaged(true);
            saveSubscriptionButton.setText("Update Membership");
            saveSubscriptionButton.setVisible(true);
            saveSubscriptionButton.setManaged(true);
            if (user.getCreditCard() != null) {
                cardNumberField.setText(user.getCreditCard());
            }
        } else {
            membershipStatusLabel.setText("No Membership");
            membershipDaysLeftLabel.setText("-");
            benefitsBox.setVisible(true);
            benefitsBox.setManaged(true);
            subscribeButton.setVisible(true);
            subscribeButton.setManaged(true);
            saveSubscriptionButton.setVisible(false);
            saveSubscriptionButton.setManaged(false);
        }
    }

    private LocalDate parseExpiry(String rawExpiry) {
        if (rawExpiry == null) {
            return null;
        }
        String trimmed = rawExpiry.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(trimmed);
        } catch (Exception first) {
            try {
                return java.time.LocalDateTime.parse(trimmed).toLocalDate();
            } catch (Exception ignored) {
                if (trimmed.length() >= 10) {
                    try {
                        return LocalDate.parse(trimmed.substring(0, 10));
                    } catch (Exception finalIgnored) {
                        return null;
                    }
                }
                return null;
            }
        }
    }

    private void setupCardValidation() {
        // Card number: digits only up to 16
        cardNumberField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*") || newVal.length() > 16) {
                cardNumberField.setText(oldVal);
            }
        });
        cvvField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*") || newVal.length() > 4) {
                cvvField.setText(oldVal);
            }
        });
    }

    private void populateExpiryCombos() {
        monthCombo.setItems(FXCollections.observableArrayList());
        for (int m = 1; m <= 12; m++) monthCombo.getItems().add(String.valueOf(m));
        yearCombo.setItems(FXCollections.observableArrayList());
        for (int y = LocalDate.now().getYear(); y <= LocalDate.now().getYear() + 15; y++) yearCombo.getItems().add(String.valueOf(y));
    }

    @FXML
    private void handleSubscribe() {
        // Show subscription form
        saveSubscriptionButton.setVisible(true);
        saveSubscriptionButton.setManaged(true);
        subscribeButton.setVisible(false);
        subscribeButton.setManaged(false);
    }

    @FXML
    private void handleSaveSubscription() {
        errorLabel.setText("");
        if (!validateSubscriptionForm()) return;
        try {
            UserDTO user = getLoggedInUser();
            if (user == null) return;
            
            // Create a fresh DTO with only the fields we want to update
            UserDTO updateRequest = new UserDTO();
            updateRequest.setId(user.getId());
            updateRequest.setUsername(user.getUsername());
            updateRequest.setFullName(user.getFullName());
            updateRequest.setEmail(user.getEmail());
            updateRequest.setPhone(user.getPhone());
            updateRequest.setRole(user.getRole());
            updateRequest.setStoreId(user.getStoreId());
            updateRequest.setActive(true);  // Keep user active
            updateRequest.setOnline(true);  // Keep user online
            
            // Update membership fields
            updateRequest.setAccountType("MEMBER");
            updateRequest.setMembershipExpiry(LocalDate.now().plusMonths(1).toString());
            updateRequest.setCreditCard(cardNumberField.getText());
            
            UserDTO updated = ApiService.updateUser(updateRequest);
            if (updated != null) {
                loggedInUser = updated; // update global session
                showSuccess("Subscribed", "Membership activated. Enjoy your benefits!");
                renderState(updated);
            } else {
                showError("Update Failed", "Could not update user.");
            }
        } catch (IOException e) {
            showError("Connection Error", e.getMessage());
        }
    }

    private boolean validateSubscriptionForm() {
        if (cardNumberField.getText().length() != 16) {
            errorLabel.setText("Card must be 16 digits");
            return false;
        }
        if (monthCombo.getValue() == null || yearCombo.getValue() == null) {
            errorLabel.setText("Select expiry month and year");
            return false;
        }
        if (cvvField.getText().length() < 3 || cvvField.getText().length() > 4) {
            errorLabel.setText("CVV must be 3-4 digits");
            return false;
        }
        return true;
    }

    @FXML
    private void handleBackToCatalog() {
        navigateToWithSize("/com/lilach/client/views/catalog.fxml", "Lilach Flower Shop Catalog", 1200, 800);
    }
}
