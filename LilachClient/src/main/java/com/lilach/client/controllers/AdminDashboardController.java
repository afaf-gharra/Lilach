package com.lilach.client.controllers;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.kordamp.ikonli.javafx.FontIcon;

import com.lilach.client.models.StoreDTO;
import com.lilach.client.models.UserDTO;
import com.lilach.client.services.ApiService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

public class AdminDashboardController extends BaseController {

    // Store Management
    @FXML private TableView<StoreDTO> storesTable;
    @FXML private TableColumn<StoreDTO, Integer> storeIdColumn;
    @FXML private TableColumn<StoreDTO, String> storeNameColumn;
    @FXML private TableColumn<StoreDTO, String> storeAddressColumn;
    @FXML private TableColumn<StoreDTO, String> storePhoneColumn;
    @FXML private TableColumn<StoreDTO, String> storeEmailColumn;
    @FXML private TableColumn<StoreDTO, Boolean> storeActiveColumn;
    @FXML private TableColumn<StoreDTO, Void> storeActionsColumn;

    @FXML private TextField storeNameField;
    @FXML private TextArea storeAddressField;
    @FXML private TextField storePhoneField;
    @FXML private TextField storeEmailField;
    @FXML private CheckBox storeActiveCheckbox;
    @FXML private Button addStoreButton;
    @FXML private Button updateStoreButton;
    @FXML private Button clearStoreButton;

    // User Management
    @FXML private TableView<UserDTO> usersTable;
    @FXML private TableColumn<UserDTO, Integer> userIdColumn;
    @FXML private TableColumn<UserDTO, String> userUsernameColumn;
    @FXML private TableColumn<UserDTO, String> userFullNameColumn;
    @FXML private TableColumn<UserDTO, String> userEmailColumn;
    @FXML private TableColumn<UserDTO, String> userRoleColumn;
    @FXML private TableColumn<UserDTO, String> userAccountTypeColumn;
    @FXML private TableColumn<UserDTO, Boolean> userActiveColumn;
    @FXML private TableColumn<UserDTO, Void> userActionsColumn;

    @FXML private TextField userUsernameField;
    @FXML private TextField userFullNameField;
    @FXML private TextField userEmailField;
    @FXML private TextField userPhoneField;
    @FXML private ComboBox<String> userRoleCombo;
    @FXML private ComboBox<String> userAccountTypeCombo;
    @FXML private ComboBox<StoreDTO> userStoreCombo;
    @FXML private CheckBox userActiveCheckbox;
    @FXML private Button addUserButton;
    @FXML private Button updateUserButton;
    @FXML private Button clearUserButton;

    // Statistics
    @FXML private Label totalStoresLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label activeStoresLabel;
    @FXML private Label activeUsersLabel;

    private ObservableList<StoreDTO> stores = FXCollections.observableArrayList();
    private ObservableList<UserDTO> users = FXCollections.observableArrayList();
    private StoreDTO selectedStore;
    private UserDTO selectedUser;

    @FXML
    public void initialize() {
        setupStoreManagement();
        setupUserManagement();
        loadData();
        loadStatistics();
    }

    private void setupStoreManagement() {
        // Store table setup
        storeIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        storeNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        storeAddressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        storePhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        storeEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        storeActiveColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        storeActiveColumn.setCellFactory(column -> new TableCell<StoreDTO, Boolean>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                setText(empty || active == null ? "" : active ? "Active" : "Inactive");
                setStyle(empty || active == null ? "" : active ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
            }
        });

        storeActionsColumn.setCellFactory(param -> new TableCell<StoreDTO, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-primary");
                deleteBtn.getStyleClass().add("btn-danger");
                editBtn.setGraphic(new FontIcon("fas-edit"));
                deleteBtn.setGraphic(new FontIcon("fas-trash"));

                editBtn.setOnAction(event -> {
                    StoreDTO store = getTableView().getItems().get(getIndex());
                    selectStore(store);
                });

                deleteBtn.setOnAction(event -> {
                    StoreDTO store = getTableView().getItems().get(getIndex());
                    deleteStore(store);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        // Store selection listener
        storesTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> selectStore(newSelection));

        // Button actions
        addStoreButton.setOnAction(e -> addStore());
        updateStoreButton.setOnAction(e -> updateStore());
        clearStoreButton.setOnAction(e -> clearStoreForm());
    }

    private void setupUserManagement() {
        // User table setup
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        userUsernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        userFullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        userEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        userRoleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        userAccountTypeColumn.setCellValueFactory(new PropertyValueFactory<>("accountType"));
        userActiveColumn.setCellValueFactory(new PropertyValueFactory<>("active"));
        userActiveColumn.setCellFactory(column -> new TableCell<UserDTO, Boolean>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                setText(empty || active == null ? "" : active ? "Active" : "Inactive");
                setStyle(empty || active == null ? "" : active ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
            }
        });

        userActionsColumn.setCellFactory(param -> new TableCell<UserDTO, Void>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.getStyleClass().add("btn-primary");
                deleteBtn.getStyleClass().add("btn-danger");
                editBtn.setGraphic(new FontIcon("fas-edit"));
                deleteBtn.setGraphic(new FontIcon("fas-trash"));

                editBtn.setOnAction(event -> {
                    UserDTO user = getTableView().getItems().get(getIndex());
                    selectUser(user);
                });

                deleteBtn.setOnAction(event -> {
                    UserDTO user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        // Setup comboboxes
        userRoleCombo.getItems().addAll("CUSTOMER", "STORE_MANAGER", "NETWORK_ADMIN");
        userAccountTypeCombo.getItems().addAll("STORE", "CHAIN", "MEMBER");

        // User selection listener
        usersTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> selectUser(newSelection));

        // Button actions
        addUserButton.setOnAction(e -> addUser());
        updateUserButton.setOnAction(e -> updateUser());
        clearUserButton.setOnAction(e -> clearUserForm());
    }

    private void loadData() {
        String accountType = getLoggedInUser().getAccountType();
        try {
            if ("CHAIN".equals(accountType) || "MEMBER".equals(accountType)) {
                // Load all stores and users for chain/member admins
                List<StoreDTO> allStores = ApiService.getAllStores();
                List<UserDTO> allUsers = ApiService.getAllUsers();
                stores.setAll(allStores);
                users.setAll(allUsers);
                userStoreCombo.getItems().setAll(allStores);
            } else {
                // Load only data for admin's store
                Integer storeId = getLoggedInUser().getStoreId();
                StoreDTO store = ApiService.getStoreById(storeId);
                List<UserDTO> allUsers = ApiService.getAllUsers();
                List<UserDTO> storeUsers = allUsers.stream()
                    .filter(u -> u.getStoreId() != null && u.getStoreId().equals(storeId))
                    .collect(Collectors.toList());

                if (store != null) {
                    stores.setAll(store);
                    users.setAll(storeUsers);
                    userStoreCombo.getItems().setAll(store);
                } else {
                    // No store assigned: empty lists
                    stores.clear();
                    users.clear();
                    userStoreCombo.getItems().clear();
                }
            }

            // Update table views
            storesTable.setItems(stores);
            usersTable.setItems(users);
            loadStatistics();

        } catch (IOException e) {
            showError("Connection Error", "Failed to load data: " + e.getMessage());
        }
    }

    private void loadStoreCombo() {
        try {
            String accountType = getLoggedInUser().getAccountType();
            if ("CHAIN".equals(accountType) || "MEMBER".equals(accountType)) {
                List<StoreDTO> allStores = ApiService.getAllStores();
                userStoreCombo.getItems().setAll(allStores);
            } else {
                StoreDTO store = ApiService.getStoreById(getLoggedInUser().getStoreId());
                if (store != null) userStoreCombo.getItems().setAll(store);
                else userStoreCombo.getItems().clear();
            }
        } catch (IOException e) {
            showError("Connection Error", "Failed to load stores for combo: " + e.getMessage());
        }
    }

    private void loadStatistics() {
        try {
            // Update statistics
            totalStoresLabel.setText(String.valueOf(stores.size()));
            totalUsersLabel.setText(String.valueOf(users.size()));

            long activeStores = stores.stream().filter(StoreDTO::isActive).count();
            long activeUsers = users.stream().filter(UserDTO::isActive).count();

            activeStoresLabel.setText(String.valueOf(activeStores));
            activeUsersLabel.setText(String.valueOf(activeUsers));

        } catch (Exception e) {
            showError("Error", "Failed to load statistics: " + e.getMessage());
        }
    }

    private void selectStore(StoreDTO store) {
        selectedStore = store;
        if (store == null) {
            clearStoreForm();
            return;
        }

        storeNameField.setText(store.getName());
        storeAddressField.setText(store.getAddress());
        storePhoneField.setText(store.getPhone());
        storeEmailField.setText(store.getEmail());
        storeActiveCheckbox.setSelected(store.isActive());

        updateStoreButton.setDisable(false);
        addStoreButton.setDisable(true);
    }

    private void selectUser(UserDTO user) {
        selectedUser = user;
        if (user == null) {
            clearUserForm();
            return;
        }

        userUsernameField.setText(user.getUsername());
        userFullNameField.setText(user.getFullName());
        userEmailField.setText(user.getEmail());
        userPhoneField.setText(user.getPhone());
        userRoleCombo.setValue(user.getRole());
        userAccountTypeCombo.setValue(user.getAccountType());
        userActiveCheckbox.setSelected(user.isActive());

        // Select store in combo
        if (user.getStoreId() != null) {
            userStoreCombo.getItems().stream()
                .filter(store -> store.getId() == user.getStoreId())
                .findFirst()
                .ifPresent(userStoreCombo::setValue);
        }

        updateUserButton.setDisable(false);
        addUserButton.setDisable(true);
    }

    private void clearStoreForm() {
        storeNameField.clear();
        storeAddressField.clear();
        storePhoneField.clear();
        storeEmailField.clear();
        storeActiveCheckbox.setSelected(true);

        selectedStore = null;
        updateStoreButton.setDisable(true);
        addStoreButton.setDisable(false);
    }

    private void clearUserForm() {
        userUsernameField.clear();
        userFullNameField.clear();
        userEmailField.clear();
        userPhoneField.clear();
        userRoleCombo.getSelectionModel().clearSelection();
        userAccountTypeCombo.getSelectionModel().clearSelection();
        userStoreCombo.getSelectionModel().clearSelection();
        userActiveCheckbox.setSelected(true);

        selectedUser = null;
        updateUserButton.setDisable(true);
        addUserButton.setDisable(false);
    }

    private void addStore() {
        if (!validateStoreForm()) {
            return;
        }

        try {
            StoreDTO newStore = createStoreFromForm();
            StoreDTO createdStore = ApiService.createStore(newStore);

            if (createdStore != null) {
                stores.add(createdStore);
                clearStoreForm();
                loadStatistics();
                showSuccess("Store Added", "Store created successfully!");
            } else {
                showError("Add Failed", "Failed to create store");
            }
        } catch (IOException e) {
            showError("Connection Error", "Failed to create store: " + e.getMessage());
        }
    }

    private void updateStore() {
        if (selectedStore == null || !validateStoreForm()) {
            return;
        }

        try {
            StoreDTO updatedStore = createStoreFromForm();
            updatedStore.setId(selectedStore.getId());

            StoreDTO result = ApiService.updateStore(updatedStore);
            if (result != null) {
                int index = stores.indexOf(selectedStore);
                if (index >= 0) {
                    stores.set(index, result);
                    storesTable.refresh();
                }
                showSuccess("Store Updated", "Store updated successfully!");
            } else {
                showError("Update Failed", "Failed to update store");
            }
        } catch (IOException e) {
            showError("Connection Error", "Failed to update store: " + e.getMessage());
        }
    }

    private void deleteStore(StoreDTO store) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Store");
        confirmation.setHeaderText("Delete " + store.getName() + "?");
        confirmation.setContentText("This will also remove all associated products and orders. This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean deleted = ApiService.deleteStore(store.getId());
                    if (deleted) {
                        stores.remove(store);
                        loadStatistics();
                        showSuccess("Store Deleted", "Store deleted successfully!");
                    } else {
                        showError("Delete Failed", "Failed to delete store");
                    }
                } catch (IOException e) {
                    showError("Connection Error", "Failed to delete store: " + e.getMessage());
                }
            }
        });
    }

    private void addUser() {
        if (!validateUserForm()) {
            return;
        }

        try {
            UserDTO newUser = createUserFromForm();
            UserDTO createdUser = ApiService.createUser(newUser);

            if (createdUser != null) {
                users.add(createdUser);
                clearUserForm();
                loadStatistics();
                showSuccess("User Added", "User created successfully!");
            } else {
                showError("Add Failed", "Failed to create user");
            }
        } catch (IOException e) {
            showError("Connection Error", "Failed to create user: " + e.getMessage());
        }
    }

    private void updateUser() {
        if (selectedUser == null || !validateUserForm()) {
            return;
        }

        try {
            UserDTO updatedUser = createUserFromForm();
            updatedUser.setId(selectedUser.getId());

            UserDTO result = ApiService.updateUser(updatedUser);
            if (result != null) {
                int index = users.indexOf(selectedUser);
                if (index >= 0) {
                    users.set(index, result);
                    usersTable.refresh();
                }
                showSuccess("User Updated", "User updated successfully!");
            } else {
                showError("Update Failed", "Failed to update user");
            }
        } catch (IOException e) {
            showError("Connection Error", "Failed to update user: " + e.getMessage());
        }
    }

    private void deleteUser(UserDTO user) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete User");
        confirmation.setHeaderText("Delete " + user.getUsername() + "?");
        confirmation.setContentText("This action cannot be undone.");

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean deleted = ApiService.deleteUser(user.getId());
                    if (deleted) {
                        users.remove(user);
                        loadStatistics();
                        showSuccess("User Deleted", "User deleted successfully!");
                    } else {
                        showError("Delete Failed", "Failed to delete user");
                    }
                } catch (IOException e) {
                    showError("Connection Error", "Failed to delete user: " + e.getMessage());
                }
            }
        });
    }

    private boolean validateStoreForm() {
        if (storeNameField.getText().isEmpty()) {
            showError("Validation Error", "Store name is required");
            return false;
        }
        if (storeAddressField.getText().isEmpty()) {
            showError("Validation Error", "Address is required");
            return false;
        }
        return true;
    }

    private boolean validateUserForm() {
        if (userUsernameField.getText().isEmpty()) {
            showError("Validation Error", "Username is required");
            return false;
        }
        if (userFullNameField.getText().isEmpty()) {
            showError("Validation Error", "Full name is required");
            return false;
        }
        if (userRoleCombo.getValue() == null) {
            showError("Validation Error", "Role is required");
            return false;
        }
        return true;
    }

    private StoreDTO createStoreFromForm() {
        StoreDTO store = new StoreDTO();
        store.setName(storeNameField.getText());
        store.setAddress(storeAddressField.getText());
        store.setPhone(storePhoneField.getText());
        store.setEmail(storeEmailField.getText());
        store.setActive(storeActiveCheckbox.isSelected());
        return store;
    }

    private UserDTO createUserFromForm() {
        UserDTO user = new UserDTO();
        user.setUsername(userUsernameField.getText());
        user.setFullName(userFullNameField.getText());
        user.setEmail(userEmailField.getText());
        user.setPhone(userPhoneField.getText());
        user.setRole(userRoleCombo.getValue());
        user.setAccountType(userAccountTypeCombo.getValue());
        user.setActive(userActiveCheckbox.isSelected());

        // Set store if selected
        StoreDTO selectedStore = userStoreCombo.getValue();
        if (selectedStore != null) {
            user.setStoreId(selectedStore.getId());
        }

        return user;
    }

    @FXML
    private void handleRefresh() {
        loadData();
        loadStatistics();
        showSuccess("Refreshed", "Data refreshed successfully");
    }

    @FXML
    private void handleLogout() {
        logout();
    }

    @FXML
    private void handleExportData() {
        // Implement data export functionality

        showSuccess("Export", "Data export functionality not yet implemented.");
        
    }

}