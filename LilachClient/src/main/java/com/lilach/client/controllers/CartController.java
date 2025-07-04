package com.lilach.client.controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;

public class CartController {
    @FXML private TableView<CartItem> cartTable;
    @FXML private Label totalPriceLabel;
    
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private double total = 0.0;
    
    @FXML
    public void initialize() {
        // Setup table columns
        TableColumn<CartItem, String> nameCol = new TableColumn<>("Product");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<CartItem, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setCellFactory(col -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", price));
                }
            }
        });
        
        TableColumn<CartItem, Integer> qtyCol = new TableColumn<>("Quantity");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        TableColumn<CartItem, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("itemTotal"));
        totalCol.setCellFactory(col -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", total));
                }
            }
        });
        
        TableColumn<CartItem, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(new RemoveButtonCellFactory());
        
        cartTable.getColumns().addAll(nameCol, priceCol, qtyCol, totalCol, actionCol);
        
        // Add sample items
        cartItems.add(new CartItem("Red Roses Bouquet", 45.00, 1));
        cartItems.add(new CartItem("Custom Flower Arrangement", 80.00, 1));
        
        cartTable.setItems(cartItems);
        calculateTotal();
    }
    
    private void calculateTotal() {
        total = cartItems.stream()
                .mapToDouble(CartItem::getItemTotal)
                .sum();
        totalPriceLabel.setText(String.format("$%.2f", total));
    }
    
    @FXML
    private void handleContinueShopping() {
        navigateToCatalog();
    }
    
    @FXML
    private void handleCheckout() {
        navigateToCheckout();
    }
    
    private void navigateToCatalog() {
        try {
            Stage stage = (Stage) cartTable.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/lilach/client/views/catalog.fxml"));
            stage.setScene(new Scene(root, 1200, 800));
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Navigation Error", "Failed to load catalog view: " + e.getMessage());
        }
    }
    
    private void navigateToCheckout() {
        try {
            Stage stage = (Stage) cartTable.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/lilach/client/views/checkout.fxml"));
            stage.setScene(new Scene(root, 1000, 700));
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Navigation Error", "Failed to load checkout view: " + e.getMessage());
        }
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Custom cell factory for remove button
    public class RemoveButtonCellFactory implements Callback<TableColumn<CartItem, Void>, TableCell<CartItem, Void>> {
        @Override
        public TableCell<CartItem, Void> call(final TableColumn<CartItem, Void> param) {
            return new TableCell<CartItem, Void>() {
                private final Button btn = new Button("Remove");
                
                {
                    btn.getStyleClass().add("btn-danger");
                    btn.setOnAction(event -> {
                        CartItem item = getTableView().getItems().get(getIndex());
                        cartItems.remove(item);
                        calculateTotal();
                    });
                }
                
                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(btn);
                    }
                }
            };
        }
    }
    
    // Cart item model
    public static class CartItem {
        private final SimpleStringProperty name;
        private final SimpleDoubleProperty price;
        private final SimpleIntegerProperty quantity;
        private final SimpleDoubleProperty itemTotal;
        
        public CartItem(String name, double price, int quantity) {
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleDoubleProperty(price);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.itemTotal = new SimpleDoubleProperty(price * quantity);
        }
        
        // Getters
        public String getName() { return name.get(); }
        public double getPrice() { return price.get(); }
        public int getQuantity() { return quantity.get(); }
        public double getItemTotal() { return itemTotal.get(); }
    }
}