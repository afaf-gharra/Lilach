package com.lilach.client.controllers;

import com.lilach.client.models.OrderDTO;
import com.lilach.client.models.ProductDTO;
import com.lilach.client.models.StoreDTO;
import com.lilach.client.services.ApiService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class StoreManagerController extends BaseController {
    
    // Order Management Tab
    @FXML private TableView<OrderDTO> ordersTable;
    @FXML private TableColumn<OrderDTO, Integer> orderIdColumn;
    @FXML private TableColumn<OrderDTO, String> customerNameColumn;
    @FXML private TableColumn<OrderDTO, String> orderDateColumn;
    @FXML private TableColumn<OrderDTO, String> deliveryDateColumn;
    @FXML private TableColumn<OrderDTO, Double> totalColumn;
    @FXML private TableColumn<OrderDTO, String> statusColumn;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TextArea orderDetailsArea;
    @FXML private ComboBox<String> statusUpdateCombo;
    @FXML private Button updateStatusButton;
    
    // Product Management Tab
    @FXML private TableView<ProductDTO> productsTable;
    @FXML private TableColumn<ProductDTO, String> productNameColumn;
    @FXML private TableColumn<ProductDTO, String> categoryColumn;
    @FXML private TableColumn<ProductDTO, Double> priceColumn;
    @FXML private TableColumn<ProductDTO, Integer> stockColumn;
    @FXML private TableColumn<ProductDTO, Boolean> availableColumn;
    @FXML private TextField productNameField;
    @FXML private TextField productCategoryField;
    @FXML private TextField productPriceField;
    @FXML private TextField productStockField;
    @FXML private TextArea productDescriptionField;
    @FXML private TextField productColorField;
    @FXML private TextField productImageField;
    @FXML private CheckBox productAvailableCheckbox;
    @FXML private Button addProductButton;
    @FXML private Button updateProductButton;
    @FXML private Button deleteProductButton;
    
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
            (obs, oldSelection, newSelection) -> showOrderDetails(newSelection));
        
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
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        availableColumn.setCellValueFactory(new PropertyValueFactory<>("available"));
        availableColumn.setCellFactory(column -> new TableCell<ProductDTO, Boolean>() {
            @Override
            protected void updateItem(Boolean available, boolean empty) {
                super.updateItem(available, empty);
                setText(empty || available == null ? "" : available ? "Yes" : "No");
            }
        });
        
        // Product selection listener
        productsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> selectProduct(newSelection));
        
        // Button actions
        addProductButton.setOnAction(e -> addProduct());
        updateProductButton.setOnAction(e -> updateProduct());
        deleteProductButton.setOnAction(e -> deleteProduct());
        
        // Form validation
        setupProductFormValidation();
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
    }
    
    private void loadStoreData() {
        Integer storeId = LoginController.loggedInUser.getStoreId();
        loadOrders(storeId);
        loadProducts(storeId);
    }
    
    private void loadOrders(Integer storeId) {
            Integer managerId = LoginController.loggedInUser.getId();
            
            
            try {
                currentStore = ApiService.getStoreByManager(managerId);
                if (currentStore == null) {
                    showError("Store Error", "No store assigned to this manager");
                    return;
                }
                
                // Update window title with store name
                Stage stage = (Stage) ordersTable.getScene().getWindow();
                stage.setTitle("Store Manager - " + currentStore.getName());
                
                loadOrders(currentStore.getId());
                loadProducts(currentStore.getId());
                
            } catch (IOException e) {
                showError("Connection Error", "Failed to load store data: " + e.getMessage());
            }
        }

        private void loadProducts(int storeId) {
        try {
            List<ProductDTO> storeProducts = ApiService.getStoreProducts(storeId);
            products.setAll(storeProducts);
            productsTable.setItems(products);
            
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
    
    private void loadProducts(Integer storeId) {
        try {
            // In a real app, you'd have an API endpoint for store products
            List<ProductDTO> allProducts = ApiService.getStoreProducts(storeId);
            products.setAll(allProducts);
            productsTable.setItems(products);
            
        } catch (IOException e) {
            showError("Connection Error", "Failed to load products: " + e.getMessage());
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
    
    private void showOrderDetails(OrderDTO order) {
        selectedOrder = order;
        if (order == null) {
            orderDetailsArea.setText("");
            statusUpdateCombo.setDisable(true);
            updateStatusButton.setDisable(true);
            return;
        }
        
        StringBuilder details = new StringBuilder();
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
                var item = order.getItems().get(i);
                details.append(i + 1).append(". ");
                if (item.getProductId() != null) {
                    details.append("Product ID: ").append(item.getProductId());
                } else {
                    details.append("Custom: ").append(item.getCustomType());
                    if (item.getCustomColor() != null) {
                        details.append(" (").append(item.getCustomColor()).append(")");
                    }
                }
                details.append(" - Qty: ").append(item.getQuantity()).append("\n");
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
    
    private ProductDTO createProductFromForm() {
        ProductDTO product = new ProductDTO();
        product.setName(productNameField.getText());
        product.setCategory(productCategoryField.getText());
        product.setPrice(Double.parseDouble(productPriceField.getText()));
        product.setStock(Integer.parseInt(productStockField.getText()));
        product.setDescription(productDescriptionField.getText());
        product.setColor(productColorField.getText());
        product.setImageUrl(productImageField.getText());
        product.setAvailable(productAvailableCheckbox.isSelected());
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
}