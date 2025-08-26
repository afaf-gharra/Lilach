package com.lilach.client.controllers;

import com.lilach.client.models.StoreDTO;
import com.lilach.client.models.UserDTO;
import com.lilach.client.services.ApiService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.List;

public class RegisterController extends BaseController {
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<StoreDTO> storeCombo;
    @FXML private TextField creditCardField;
    @FXML private TextField expiryDateField;
    @FXML private TextField cvvField;
    @FXML private CheckBox subscriptionCheckbox;
    @FXML private CheckBox termsCheckbox;
    @FXML private Button registerButton;
    @FXML private Hyperlink loginLink;
    
    private List<StoreDTO> allStores;
    
    @FXML
    public void initialize() {
        setupFormValidation();
        loadStores();
        setupEventHandlers();
    }
    
    private void setupFormValidation() {
         phoneField.setTextFormatter(new TextFormatter<String>(change -> {
            if (change.getControlNewText().matches("\\d{0,10}")) {
                return change;
            }
            return null;
        }));

        // Credit card validation
        creditCardField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                creditCardField.setText(oldValue);
            } else if (newValue.length() > 16) {
                creditCardField.setText(oldValue);
            }
        });
        
        // Expiry date validation (MM/YY)
        expiryDateField.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d{0,2}/?\\d{0,2}")) {
                // Auto-insert slash after 2 digits
                if (newText.length() == 2 && !newText.contains("/")) {
                    change.setText(change.getText() + "/");
                }
                return change;
            }
            return null;
        }));
        
        // CVV validation
        cvvField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                cvvField.setText(oldValue);
            } else if (newValue.length() > 4) {
                cvvField.setText(oldValue);
            }
        });
    }
    
    private void loadStores() {
        try {
            allStores = ApiService.getAllStores();
            storeCombo.setItems(FXCollections.observableArrayList(allStores));
            
            // Set custom cell factory to show store names
            storeCombo.setCellFactory(param -> new ListCell<StoreDTO>() {
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
            storeCombo.setButtonCell(new ListCell<StoreDTO>() {
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
    
    private void setupEventHandlers() {
        registerButton.setOnAction(e -> handleRegister());
        loginLink.setOnAction(e -> navigateToLogin());
        
        // Subscription checkbox tooltip
        subscriptionCheckbox.setTooltip(new Tooltip("Get 10% discount and free delivery for $20/month"));
    }
    
    @FXML
    private void handleRegister() {
        if (validateForm()) {
            UserDTO newUser = createUserFromForm();
            
            try {
                UserDTO createdUser = ApiService.register(newUser);
                if (createdUser != null) {
                    // Save user data and auto-login
                    loggedInUser = createdUser;
                    
                    showSuccess("Account Created", "Your account has been created successfully!");
                    navigateToCatalog();
                } else {
                    showError("Registration Failed", "Failed to create account. Please try again.");
                }
            } catch (IOException e) {
                showError("Connection Error", "Failed to connect to server: " + e.getMessage());
            }
        }
    }
    
    private boolean validateForm() {
        // Basic validation
        if (fullNameField.getText().isEmpty()) {
            showError("Validation Error", "Full name is required");
            return false;
        }
        if (usernameField.getText().isEmpty()) {
            showError("Validation Error", "Username is required");
            return false;
        }
        if (passwordField.getText().isEmpty()) {
            showError("Validation Error", "Password is required");
            return false;
        }
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Validation Error", "Passwords do not match");
            return false;
        }
        if (emailField.getText().isEmpty()) {
            showError("Validation Error", "Email is required");
            return false;
        }
        if (storeCombo.getValue() == null) {
            showError("Validation Error", "Please select a preferred store");
            return false;
        }
        
        if (!validatePhoneNumber(phoneField.getText())) {
            showError("Error","Phone number must be exactly 10 digits");
            return false;
        }
        // Payment validation
        if (creditCardField.getText().length() != 16) {
            showError("Validation Error", "Please enter a valid 16-digit credit card number");
            return false;
        }
        if (!expiryDateField.getText().matches("\\d{2}/\\d{2}")) {
            showError("Validation Error", "Please enter expiry date in MM/YY format");
            return false;
        }
        if (cvvField.getText().length() < 3 || cvvField.getText().length() > 4) {
            showError("Validation Error", "Please enter a valid CVV (3-4 digits)");
            return false;
        }
        if (!termsCheckbox.isSelected()) {
            showError("Validation Error", "You must agree to terms and conditions");
            return false;
        }
        
        return true;
    }
    
    private boolean validatePhoneNumber(String phone) {
        return phone != null && phone.matches("\\d{10}");
    }
    private UserDTO createUserFromForm() {
        UserDTO user = new UserDTO();
        user.setFullName(fullNameField.getText());
        user.setUsername(usernameField.getText());
        user.setPassword(passwordField.getText());
        user.setEmail(emailField.getText());
        user.setPhone(phoneField.getText());
        user.setCreditCard(creditCardField.getText());
        
        // Set store from selection
        StoreDTO selectedStore = storeCombo.getValue();
        user.setStoreId(selectedStore.getId());
        
        // Set account type based on subscription
        if (subscriptionCheckbox.isSelected()) {
            user.setAccountType("MEMBER");
        } else {
            user.setAccountType("CUSTOMER");
        }
        
        user.setRole("CUSTOMER");
        user.setActive(true);
        
        return user;
    }
    
    private void navigateToLogin() {
        navigateTo("/com/lilach/client/views/login.fxml", "Login");
    }
    
    private void navigateToCatalog() {
        navigateToWithSize("/com/lilach/client/views/catalog.fxml", "Lilach Flower Shop Catalog", 1200, 800);
    }
}