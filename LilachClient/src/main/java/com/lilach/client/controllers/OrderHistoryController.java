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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;

public class OrderHistoryController extends BaseController {
    @FXML private TableView<Order> ordersTable;
    
    private ObservableList<Order> orders = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        // Setup table columns
        TableColumn<Order, Integer> idCol = new TableColumn<>("Order ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Order, String> orderDateCol = new TableColumn<>("Order Date");
        orderDateCol.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
        
        TableColumn<Order, String> deliveryDateCol = new TableColumn<>("Delivery Date");
        deliveryDateCol.setCellValueFactory(new PropertyValueFactory<>("deliveryDate"));
        
        TableColumn<Order, String> recipientCol = new TableColumn<>("Recipient");
        recipientCol.setCellValueFactory(new PropertyValueFactory<>("recipient"));
        
        TableColumn<Order, Double> totalCol = new TableColumn<>("Total Price");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        totalCol.setCellFactory(col -> new TableCell<Order, Double>() {
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
        
        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        TableColumn<Order, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(new ActionButtonCellFactory());
        
        ordersTable.getColumns().addAll(idCol, orderDateCol, deliveryDateCol, recipientCol, totalCol, statusCol, actionCol);
        
        // Add sample orders
        orders.add(new Order(1, "2025-07-01", "2025-07-02", "Jane Smith", 125.00, "Delivered"));
        orders.add(new Order(2, "2025-07-10", "2025-07-12", "John Doe", 95.00, "Processing"));
        
        ordersTable.setItems(orders);
    }
    
    @FXML
    private void handleBackToCatalog() {
        navigateToCatalog();
    }
    
    @FXML
    private void handleCreateComplaint() {
        navigateToComplaint();
    }
    

    
    private void navigateToComplaint() {
        try {
            Stage stage = (Stage) ordersTable.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/lilach/client/views/complaint.fxml"));
            stage.setScene(new Scene(root, 800, 600));
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Navigation Error", "Failed to load complaint view: " + e.getMessage());
        }
    }

    
    // Order model
    public static class Order {
        private final SimpleIntegerProperty id;
        private final SimpleStringProperty orderDate;
        private final SimpleStringProperty deliveryDate;
        private final SimpleStringProperty recipient;
        private final SimpleDoubleProperty totalPrice;
        private final SimpleStringProperty status;
        
        public Order(int id, String orderDate, String deliveryDate, String recipient, double totalPrice, String status) {
            this.id = new SimpleIntegerProperty(id);
            this.orderDate = new SimpleStringProperty(orderDate);
            this.deliveryDate = new SimpleStringProperty(deliveryDate);
            this.recipient = new SimpleStringProperty(recipient);
            this.totalPrice = new SimpleDoubleProperty(totalPrice);
            this.status = new SimpleStringProperty(status);
        }
        
        // Getters
        public int getId() { return id.get(); }
        public String getOrderDate() { return orderDate.get(); }
        public String getDeliveryDate() { return deliveryDate.get(); }
        public String getRecipient() { return recipient.get(); }
        public double getTotalPrice() { return totalPrice.get(); }
        public String getStatus() { return status.get(); }
    }
    
    // Custom cell factory for action buttons
    public class ActionButtonCellFactory implements Callback<TableColumn<Order, Void>, TableCell<Order, Void>> {
        @Override
        public TableCell<Order, Void> call(final TableColumn<Order, Void> param) {
            return new TableCell<Order, Void>() {
                private final Button viewBtn = new Button("View");
                private final Button cancelBtn = new Button("Cancel");
                private final HBox pane = new HBox(viewBtn, cancelBtn);
                
                {
                    pane.setSpacing(5);
                    viewBtn.getStyleClass().add("btn-info");
                    cancelBtn.getStyleClass().add("btn-danger");
                    
                    viewBtn.setOnAction(event -> {
                        Order order = getTableView().getItems().get(getIndex());
                        viewOrderDetails(order);
                    });
                    
                    cancelBtn.setOnAction(event -> {
                        Order order = getTableView().getItems().get(getIndex());
                        cancelOrder(order);
                    });
                }
                
                @Override
                public void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(pane);
                        Order order = getTableView().getItems().get(getIndex());
                        cancelBtn.setDisable(!order.getStatus().equals("Processing"));
                    }
                }
            };
        }
    }
    
    private void viewOrderDetails(Order order) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Details");
        alert.setHeaderText("Order #" + order.getId());
        alert.setContentText(
            "Order Date: " + order.getOrderDate() + "\n" +
            "Delivery Date: " + order.getDeliveryDate() + "\n" +
            "Recipient: " + order.getRecipient() + "\n" +
            "Total: $" + order.getTotalPrice() + "\n" +
            "Status: " + order.getStatus()
        );
        alert.showAndWait();
    }
    
    private void cancelOrder(Order order) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Order");
        confirm.setHeaderText("Cancel Order #" + order.getId());
        confirm.setContentText("Are you sure you want to cancel this order?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showSuccess("Order Cancelled", "Order #" + order.getId() + " has been cancelled.");
                // In real app, update status in backend
            }
        });
    }
    
 
}