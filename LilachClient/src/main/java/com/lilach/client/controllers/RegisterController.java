package com.lilach.client.controllers;

import com.lilach.client.models.StoreDTO;
import com.lilach.client.models.UserDTO;
import com.lilach.client.services.ApiService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class RegisterController extends BaseController {
    @FXML private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<StoreDTO> storeCombo;
    @FXML private TextField creditCardField;
    @FXML private ComboBox<String> monthCombo;
    @FXML private ComboBox<String> yearCombo;
    @FXML private TextField cvvField;
    @FXML private CheckBox subscriptionCheckbox;
    @FXML private CheckBox termsCheckbox;
    @FXML private Button registerButton;
    @FXML private Hyperlink loginLink;

    
    private List<StoreDTO> allStores;
    // Simple email validation pattern (allows subdomains, final TLD of length >=2)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    
    @FXML
    public void initialize() {
        populateExpiryCombos();
        setupFormValidation();
        loadStores();
        setupEventHandlers();
    }
    
    private void setupFormValidation() {
       phoneField.setTextFormatter(new TextFormatter<String>(change -> {
    String newText = change.getControlNewText();

    if (newText.isEmpty()) return change;

    if (newText.equals("0") || newText.equals("05")) return change;

    if (newText.matches("05\\d{0,8}")) return change;

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
        
        // expiry month/year are provided via combo boxes; no text formatter needed
        
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
private void navigateToCatalog() {
    navigateToWithSize("/com/lilach/client/views/catalog.fxml",
            "Lilach Flower Shop Catalog", 1200, 800);
}

    // FXML onAction handler expected by register.fxml (See Catalog button)
    @FXML
    private void handleSeeCatalog() {
        navigateToCatalog();
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
        // Email format validation
        if (!EMAIL_PATTERN.matcher(emailField.getText()).matches()) {
            showError("Validation Error", "Please enter a valid email address (e.g. john.doe@gmail.com)");
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
        if (monthCombo.getValue() == null || yearCombo.getValue() == null) {
            showError("Validation Error", "Please select expiry month and year");
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
    return phone != null && phone.matches("05\\d{8}");
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
            user.setAccountType("CHAIN");
        } else {
            user.setAccountType("STORE");
        }
        
        user.setRole("CUSTOMER");
        user.setActive(true);
        
        return user;
    }
    
    private void populateExpiryCombos() {
        // Months 1-12
        ArrayList<String> months = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            months.add(String.valueOf(m));
        }
        monthCombo.setItems(FXCollections.observableArrayList(months));

        // Years 2026-2040
        ArrayList<String> years = new ArrayList<>();
        for (int y = 2026; y <= 2040; y++) {
            years.add(String.valueOf(y));
        }
        yearCombo.setItems(FXCollections.observableArrayList(years));
    }
    
    
   
}