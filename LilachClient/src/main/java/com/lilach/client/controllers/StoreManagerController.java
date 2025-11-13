package com.lilach.client.controllers;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.lilach.client.models.OrderDTO;
import com.lilach.client.models.OrderItemDTO;
import com.lilach.client.models.ProductDTO;
import com.lilach.client.models.StoreDTO;
import com.lilach.client.services.ApiService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class StoreManagerController extends BaseController {
    
    // Order Management Tab
    @FXML private TableView<OrderDTO> ordersTable;
    @FXML private TableColumn<OrderDTO, Integer> orderIdColumn;
    @FXML private TableColumn<OrderDTO, String> customerNameColumn;
    @FXML private TableColumn<OrderDTO, String> orderDateColumn;
    @FXML private TableColumn<OrderDTO, String> deliveryDateColumn;
    @FXML private TableColumn<OrderDTO, Double> totalColumn;
    @FXML private TableColumn<OrderDTO, String> statusColumn;
    @FXML private TableColumn<OrderDTO, String> deliveryTypeColumn;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TextArea orderDetailsArea;
    @FXML private ComboBox<String> statusUpdateCombo;
    @FXML private Button updateStatusButton;
    
    // Product Management Tab
    @FXML private TableView<ProductDTO> productsTable;
    @FXML private TableColumn<ProductDTO, String> productNameColumn;
    @FXML private TableColumn<ProductDTO, String> categoryColumn;
    @FXML private TableColumn<ProductDTO, Double> priceColumn;
    @FXML private TableColumn<ProductDTO, Integer> discountColumn;
    @FXML private TableColumn<ProductDTO, Integer> stockColumn;
    @FXML private TableColumn<ProductDTO, Boolean> availableColumn;
    @FXML private TextField productNameField;
    @FXML private TextField productCategoryField;
    @FXML private TextField productPriceField;
    @FXML private TextField productStockField;
    @FXML private TextArea productDescriptionField;
    @FXML private TextField productColorField;
    @FXML private TextField productImageField;
    @FXML private TextField productDiscountField;
    @FXML private CheckBox productAvailableCheckbox;
    @FXML private Button addProductButton;
    @FXML private Button updateProductButton;
    @FXML private Button deleteProductButton;
    
    // Store-wide discount controls
    @FXML private TextField storeDiscountField;
    @FXML private Button applyStoreDiscountButton;
    @FXML private ComboBox<StoreDTO> storeSelector;
    
    private ObservableList<OrderDTO> orders = FXCollections.observableArrayList();
    private ObservableList<ProductDTO> products = FXCollections.observableArrayList();
    private OrderDTO selectedOrder;
    private ProductDTO selectedProduct;
    private StoreDTO currentStore;

    
    @FXML
    public void initialize() {
        setupOrderManagement();
        setupProductManagement();
        loadStoreData();
    }
    
    private void setupOrderManagement() {
        // Order table setup
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("recipientName"));
        orderDateColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue().getOrderDate() != null ?
                cellData.getValue().getOrderDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) : ""
            )
        );
        deliveryDateColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue().getDeliveryDate() != null ?
                cellData.getValue().getDeliveryDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) : ""
            )
        );
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        totalColumn.setCellFactory(column -> new TableCell<OrderDTO, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? "" : String.format("$%.2f", price));
            }
        });
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Status filter
        statusFilterCombo.getItems().addAll("All", "PENDING", "PROCESSING", "DELIVERED", "CANCELLED");
        statusFilterCombo.setValue("PENDING");
        statusFilterCombo.setOnAction(e -> filterOrders());
        
        // Status update combo
        statusUpdateCombo.getItems().addAll("PENDING", "PROCESSING", "DELIVERED", "CANCELLED");
        
        // Order selection listener
        ordersTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                try {
                    showOrderDetails(newSelection);
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            });

        // set deliveryTypeColumn
        deliveryTypeColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue().getdeliveryType() != null ?
                cellData.getValue().getdeliveryType() : ""
            )
        );
        // Update status button
        updateStatusButton.setOnAction(e -> updateOrderStatus());
    }
    
    private void setupProductManagement() {
        // Product table setup
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setCellFactory(column -> new TableCell<ProductDTO, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? "" : String.format("$%.2f", price));
            }
        });
        discountColumn.setCellValueFactory(new PropertyValueFactory<>("discount"));
        discountColumn.setCellFactory(column -> new TableCell<ProductDTO, Integer>() {
            @Override
            protected void updateItem(Integer discount, boolean empty) {
                super.updateItem(discount, empty);
                if (empty || discount == null) {
                    setText("");
                } else {
                    setText(discount + "%");
                    if (discount > 0) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        productDiscountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (!newValue.matches("\\d*")) {
                    productDiscountField.setText(oldValue);
                } else {
                    try {
                        int value = Integer.parseInt(newValue);
                        if (value > 100) {
                            productDiscountField.setText(oldValue);
                        }
                    } catch (NumberFormatException e) {
                        // Keep the value if parsing fails (empty or incomplete)
                    }
                }
            }
        });

        // Store-wide discount input validation (0-100)
        if (storeDiscountField != null) {
            storeDiscountField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d{0,2}|100")) {
                    storeDiscountField.setText(oldVal);
                }
            });
        }

        if (applyStoreDiscountButton != null) {
            applyStoreDiscountButton.setOnAction(e -> applyStoreDiscount());
        }
        // Button actions
        addProductButton.setOnAction(e -> addProduct());
        updateProductButton.setOnAction(e -> updateProduct());
        deleteProductButton.setOnAction(e -> deleteProduct());
        
        // Form validation
        setupProductFormValidation();

        // Restore: clicking a product populates the form for editing
        if (productsTable != null) {
            productsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                selectProduct(newSel);
            });
        }

        // Initial state: nothing selected, disable update/delete
        updateProductButton.setDisable(true);
        deleteProductButton.setDisable(true);
    }
    
    private void setupProductFormValidation() {
        // Numeric validation for price and stock
        productPriceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                productPriceField.setText(oldValue);
            }
        });
        
        productStockField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                productStockField.setText(oldValue);
            }
        });
        
        // Discount validation (0-100)
        productDiscountField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.isEmpty()) {
                if (!newValue.matches("\\d*")) {
                    productDiscountField.setText(oldValue);
                } else {
                    try {
                        int value = Integer.parseInt(newValue);
                        if (value > 100) {
                            productDiscountField.setText(oldValue);
                        }
                    } catch (NumberFormatException e) {
                        // Keep the value if parsing fails (empty or incomplete)
                    }
                }
            }
        });
    }
    
    private void loadStoreData() {
        String accountType = getLoggedInUser().getAccountType();
        
        if ("CHAIN".equalsIgnoreCase(accountType) || "MEMBER".equalsIgnoreCase(accountType)) {
            // Chain/Member can see all stores
            try {
                List<StoreDTO> stores = ApiService.getAllStores();
                if (storeSelector != null) {
                    storeSelector.getItems().setAll(stores);
                    storeSelector.setVisible(true);
                    storeSelector.setManaged(true);
                    storeSelector.setOnAction(e -> handleStoreSelection());
                }
                // If only one store exists, auto-select it
                if (stores != null && stores.size() == 1) {
                    if (storeSelector != null) {
                        storeSelector.getSelectionModel().select(0);
                    }
                    currentStore = stores.get(0);
                    storeDiscountField.setText(String.valueOf(currentStore.getStoreDiscount()));
                    loadStoreOrders(currentStore.getId());
                    loadStoreProducts(currentStore.getId());
                } else {
                    // Multiple stores: load all orders/products immediately; user can filter by store if needed
                    currentStore = null;
                    loadAllOrders();
                    loadAllProducts();
                }
            } catch (IOException e) {
                showError("Connection Error", "Failed to load stores: " + e.getMessage());
            }
        } else {
            // Store employees: auto use their assigned store
            if (storeSelector != null) {
                storeSelector.setVisible(false);
                storeSelector.setManaged(false);
            }
            Integer storeId = getLoggedInUser().getStoreId();
            if (storeId == null) {
                showError("Store Error", "No store assigned to this manager");
                return;
            }
            try {
                currentStore = ApiService.getStoreById(storeId);
                if (currentStore == null) {
                    showError("Store Error", "No store found");
                    return;
                }
                if (storeDiscountField != null) {
                    storeDiscountField.setText(String.valueOf(currentStore.getStoreDiscount()));
                }
                loadStoreOrders(storeId);
                loadStoreProducts(storeId);
            } catch (IOException e) {
                showError("Connection Error", "Failed to load store: " + e.getMessage());
                return;
            }
        }
    }

    @FXML
    private void handleStoreSelection() {
        if (storeSelector == null) return;
        StoreDTO selected = storeSelector.getValue();
        if (selected == null) {
            currentStore = null;
            return;
        }
        try {
            // Refresh selected store from server in case discount changed
            StoreDTO fresh = ApiService.getStoreById(selected.getId());
            currentStore = fresh != null ? fresh : selected;
            if (storeDiscountField != null && currentStore != null) {
                storeDiscountField.setText(String.valueOf(currentStore.getStoreDiscount()));
            }
            loadStoreOrders(currentStore.getId());
            loadStoreProducts(currentStore.getId());
        } catch (IOException e) {
            showError("Connection Error", "Failed to load selected store: " + e.getMessage());
        }
    }

    @FXML
    private void applyStoreDiscount() {
        if (currentStore == null) {
            // Try to derive from selector if available
            if (storeSelector != null && storeSelector.getValue() != null) {
                currentStore = storeSelector.getValue();
            } else {
                showError("Store Error", "Please select a store first");
                return;
            }
        }
        try {
            int discount = Integer.parseInt(storeDiscountField.getText());
            if (discount < 0 || discount > 100) {
                showError("Invalid Discount", "Discount must be between 0 and 100");
                return;
            }
            // Build updates DTO
            StoreDTO updates = new StoreDTO();
            updates.setStoreDiscount(discount);
            
            // Call API to update store
            StoreDTO updated = ApiService.updateStore(currentStore.getId(), updates);
            if (updated != null) {
                currentStore = updated;
                showSuccess("Store Discount Updated", "Store-wide discount set to " + discount + "%");
                // Refresh products to reflect effective discounts
                if (currentStore != null) {
                    loadStoreProducts(currentStore.getId());
                }
            } else {
                showError("Update Failed", "Could not update store discount");
            }
        } catch (NumberFormatException e) {
            showError("Invalid Input", "Please enter a valid number for discount");
        } catch (IOException e) {
            showError("Connection Error", "Failed to update store: " + e.getMessage());
        }
    }
    
    private void loadAllOrders() {
        try {
            List<OrderDTO> allOrders = ApiService.getAllOrders();
            orders.setAll(allOrders);
            ordersTable.setItems(orders);
            filterOrders();
        } catch (IOException e) {
            showError("Connection Error", "Failed to load orders: " + e.getMessage());
        }
    }
    
    private void loadAllProducts() {
        try {
            List<ProductDTO> allProducts = ApiService.getAllProducts();
            products.setAll(allProducts);
            productsTable.setItems(products);
            // Clear selection and form when reloading
            productsTable.getSelectionModel().clearSelection();
            selectedProduct = null;
            clearProductForm();
        } catch (IOException e) {
            showError("Connection Error", "Failed to load products: " + e.getMessage());
        }
    }

    private void loadStoreOrders(int storeId) {
        try {
            List<OrderDTO> storeOrders = ApiService.getStoreOrders(storeId);
            orders.setAll(storeOrders);
            ordersTable.setItems(orders);
            filterOrders();
        } catch (IOException e) {
            showError("Connection Error", "Failed to load orders: " + e.getMessage());
        }
    }

    private void loadStoreProducts(int storeId) {
        try {
            List<ProductDTO> storeProducts = ApiService.getStoreProducts(storeId);
            products.setAll(storeProducts);
            productsTable.setItems(products);
            // Clear selection and form when reloading
            productsTable.getSelectionModel().clearSelection();
            selectedProduct = null;
            clearProductForm();
        } catch (IOException e) {
            showError("Connection Error", "Failed to load products: " + e.getMessage());
        }
    }

    // Add stock management method
    private void updateProductStock() {
        if (selectedProduct == null) {
            showError("Selection Error", "Please select a product");
            return;
        }
        
        try {
            String stockText = productStockField.getText();
            if (stockText.isEmpty()) {
                showError("Validation Error", "Please enter stock quantity");
                return;
            }
            
            int newStock = Integer.parseInt(stockText);
            boolean updated = ApiService.updateProductStock(selectedProduct.getId(), newStock);
            
            if (updated) {
                selectedProduct.setStock(newStock);
                productsTable.refresh();
                showSuccess("Stock Updated", "Product stock updated to: " + newStock);
            } else {
                showError("Update Failed", "Failed to update product stock");
            }
            
        } catch (NumberFormatException e) {
            showError("Validation Error", "Please enter a valid number for stock");
        } catch (IOException e) {
            showError("Connection Error", "Failed to update stock: " + e.getMessage());
        }
    }
    
    private void filterOrders() {
        String filter = statusFilterCombo.getValue();
        if ("All".equals(filter)) {
            ordersTable.setItems(orders);
        } else {
            ObservableList<OrderDTO> filtered = orders.stream()
                .filter(order -> filter.equals(order.getStatus()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
            ordersTable.setItems(filtered);
        }
    }
    
    private void showOrderDetails(OrderDTO order) throws IOException {
        selectedOrder = order;
        if (order == null) {
            orderDetailsArea.setText("");
            statusUpdateCombo.setDisable(true);
            updateStatusButton.setDisable(true);
            return;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("-------------> ").append(order.getdeliveryType()).append(" <-------------\n");
        details.append("Order ID: ").append(order.getId()).append("\n");
        details.append("Customer: ").append(order.getRecipientName()).append("\n");
        details.append("Phone: ").append(order.getRecipientPhone()).append("\n");
        details.append("Delivery Address: ").append(order.getDeliveryAddress()).append("\n");
        details.append("Order Date: ").append(order.getOrderDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))).append("\n");
        details.append("Delivery Date: ").append(order.getDeliveryDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))).append("\n");
        details.append("Status: ").append(order.getStatus()).append("\n");
        details.append("Total: $").append(String.format("%.2f", order.getTotalPrice())).append("\n\n");
        
        if (order.getGreetingMessage() != null && !order.getGreetingMessage().isEmpty()) {
            details.append("Greeting Message: ").append(order.getGreetingMessage()).append("\n\n");
        }
        
        details.append("Items:\n");
        if (order.getItems() != null) {
            for (int i = 0; i < order.getItems().size(); i++) {
                OrderItemDTO item = order.getItems().get(i);
                ProductDTO product = item.getProduct();
                details.append(i + 1).append(". ");
                
                // Check if this is a custom product
                if (product == null) {
                    // Custom product - display custom details
                    details.append("Custom ").append(item.getCustomType() != null ? item.getCustomType() : "Arrangement");
                    details.append("\n   Color: ").append(item.getCustomColor() != null ? item.getCustomColor() : "N/A");
                    details.append("\n   Price Range: ").append(item.getCustomPriceRange() != null ? item.getCustomPriceRange() : "N/A");
                    if (item.getCustomFlowerTypes() != null && !item.getCustomFlowerTypes().isEmpty()) {
                        details.append("\n   Flowers: ").append(item.getCustomFlowerTypes());
                    }
                    if (item.getCustomSpecialRequests() != null && !item.getCustomSpecialRequests().isEmpty()) {
                        details.append("\n   Special Requests: ").append(item.getCustomSpecialRequests());
                    }
                    details.append("\n   Qty: ").append(item.getQuantity()).append("\n");
                } else {
                    // Regular product
                    details.append(product.getName()).append(" - ");
                    details.append(String.format("$%.2f", product.getPrice())).append(" - ");
                    details.append(product.getCategory()).append(" - ");
                    details.append("Qty: ").append(item.getQuantity()).append("\n");
                }
            }
        }
        
        orderDetailsArea.setText(details.toString());
        statusUpdateCombo.setValue(order.getStatus());
        statusUpdateCombo.setDisable(false);
        updateStatusButton.setDisable(false);
    }
    
    private void updateOrderStatus() {
        if (selectedOrder == null) {
            showError("Selection Error", "Please select an order to update");
            return;
        }
        
        String newStatus = statusUpdateCombo.getValue();
        if (newStatus == null || newStatus.equals(selectedOrder.getStatus())) {
            showError("Update Error", "Please select a different status");
            return;
        }
        
        try {
            // Update order status via API
            OrderDTO updatedOrder = ApiService.updateOrderStatus(selectedOrder.getId(), newStatus);
            if (updatedOrder != null) {
                // Update local data
                selectedOrder.setStatus(newStatus);
                ordersTable.refresh();
                showSuccess("Status Updated", "Order status updated to: " + newStatus);
            } else {
                showError("Update Failed", "Failed to update order status");
            }
        } catch (IOException e) {
            showError("Connection Error", "Failed to update order: " + e.getMessage());
        }
    }

    // Add a new tab or button for complaints management
    @FXML
    private void handleViewComplaints() {
        navigateTo("/com/lilach/client/views/complaints_manager.fxml", "Complaints Management");
    }
        
    private void selectProduct(ProductDTO product) {
        selectedProduct = product;
        if (product == null) {
            clearProductForm();
            return;
        }
        
        // Fill form with product data
        productNameField.setText(product.getName());
        productCategoryField.setText(product.getCategory());
        productPriceField.setText(String.valueOf(product.getPrice()));
        productStockField.setText(String.valueOf(product.getStock()));
        productDescriptionField.setText(product.getDescription());
        productColorField.setText(product.getColor());
        productImageField.setText(product.getImageUrl());
        productAvailableCheckbox.setSelected(product.isAvailable());
        
        // Set discount field
        try {
            productDiscountField.setText(String.valueOf(product.getDiscount()));
        } catch (Exception e) {
            productDiscountField.setText("0");
        }
        
        updateProductButton.setDisable(false);
        deleteProductButton.setDisable(false);
        addProductButton.setDisable(true);
    }
    
    private void clearProductForm() {
        productNameField.clear();
        productCategoryField.clear();
        productPriceField.clear();
        productStockField.clear();
        productDescriptionField.clear();
        productColorField.clear();
        productImageField.clear();
        productDiscountField.setText("0");
        productAvailableCheckbox.setSelected(true);
        
        updateProductButton.setDisable(true);
        deleteProductButton.setDisable(true);
        addProductButton.setDisable(false);
    }
    
    private void addProduct() {
        if (!validateProductForm()) {
            return;
        }
        
        try {
            ProductDTO newProduct = createProductFromForm();
            ProductDTO createdProduct = ApiService.createProduct(newProduct);
            
            if (createdProduct != null) {
                products.add(createdProduct);
                clearProductForm();
                showSuccess("Product Added", "Product added successfully!");
            } else {
                showError("Add Failed", "Failed to add product");
            }
        } catch (IOException e) {
            showError("Connection Error", "Failed to add product: " + e.getMessage());
        }
    }
    
    private void updateProduct() {
        if (selectedProduct == null || !validateProductForm()) {
            return;
        }
        
        try {
            ProductDTO updatedProduct = createProductFromForm();
            updatedProduct.setId(selectedProduct.getId());
            
            ProductDTO result = ApiService.editProduct(updatedProduct);
            if (result != null) {
                // Update local data
                int index = products.indexOf(selectedProduct);
                if (index >= 0) {
                    products.set(index, result);
                    productsTable.refresh();
                }
                showSuccess("Product Updated", "Product updated successfully!");
            } else {
                showError("Update Failed", "Failed to update product");
            }
        } catch (IOException e) {
            showError("Connection Error", "Failed to update product: " + e.getMessage());
        }
    }
    
    private void deleteProduct() {
        if (selectedProduct == null) {
            showError("Selection Error", "Please select a product to delete");
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Product");
        confirmation.setHeaderText("Delete " + selectedProduct.getName() + "?");
        confirmation.setContentText("This action cannot be undone.");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    ApiService.deleteProduct(selectedProduct.getId());
                    products.remove(selectedProduct);
                    clearProductForm();
                    showSuccess("Product Deleted", "Product deleted successfully!");
                } catch (IOException e) {
                    showError("Connection Error", "Failed to delete product: " + e.getMessage());
                }
            }
        });
    }
    
    private boolean validateProductForm() {
        if (productNameField.getText().isEmpty()) {
            showError("Validation Error", "Product name is required");
            return false;
        }
        if (productCategoryField.getText().isEmpty()) {
            showError("Validation Error", "Category is required");
            return false;
        }
        if (productPriceField.getText().isEmpty()) {
            showError("Validation Error", "Price is required");
            return false;
        }
        if (productStockField.getText().isEmpty()) {
            showError("Validation Error", "Stock quantity is required");
            return false;
        }
        return true;
    }
    
    private ProductDTO createProductFromForm() throws IOException {
        ProductDTO product = new ProductDTO();
        product.setName(productNameField.getText());
        product.setCategory(productCategoryField.getText());
        product.setPrice(Double.parseDouble(productPriceField.getText()));
        product.setStock(Integer.parseInt(productStockField.getText()));
        product.setDescription(productDescriptionField.getText());
        product.setColor(productColorField.getText());
        product.setImageUrl(productImageField.getText());
        product.setAvailable(productAvailableCheckbox.isSelected());
        
        // Set discount (default to 0 if empty or invalid)
        try {
            String discountText = productDiscountField.getText();
            int discount = discountText.isEmpty() ? 0 : Integer.parseInt(discountText);
            product.setDiscount(Math.max(0, Math.min(100, discount))); // Clamp to 0-100
        } catch (NumberFormatException e) {
            product.setDiscount(0);
        }

        // IMPORTANT: use the currently selected store (for chain/member) or the user's store
        StoreDTO storeForProduct = null;
        if (currentStore != null) {
            storeForProduct = currentStore;
        } else if (loggedInUser != null && loggedInUser.getStoreId() != null) {
            try {
                storeForProduct = ApiService.getStoreById(loggedInUser.getStoreId());
            } catch (IOException ignore) { /* handled below */ }
        }
        if (storeForProduct == null) {
            showError("Store Error", "No store selected or assigned. Please select a store.");
            throw new IOException("No store available for product assignment");
        }
        product.setStore(storeForProduct);
        return product;
    }
    
    @FXML
    private void handleRefresh() {
        loadStoreData();
        showSuccess("Refreshed", "Data refreshed successfully");
    }
    
    @FXML
    private void handleLogout() {
        logout();
    }

    @FXML
    private void handleComplaints() {
        navigateTo("/com/lilach/client/views/complaints_manager.fxml", "Complaints Form");
    }

    @FXML
    private void handleReports() {
        navigateTo("/com/lilach/client/views/reports.fxml", "Reports");
    }
}