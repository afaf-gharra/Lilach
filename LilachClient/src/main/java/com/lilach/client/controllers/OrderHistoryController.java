package com.lilach.client.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import com.lilach.client.models.OrderDTO;
import com.lilach.client.services.ApiService;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class OrderHistoryController extends BaseController {
    @FXML private TableView<Order> ordersTable;
    
    private ObservableList<Order> orders = FXCollections.observableArrayList();
    @FXML
    private void handleViewCart() {
        navigateTo("/com/lilach/client/views/cart.fxml", "Shopping Cart");
    }
    
    @FXML
    private void handleViewOrders() {
        navigateTo("/com/lilach/client/views/order_history.fxml", "My Orders");
    }

    @FXML
    public void initialize() throws IOException {
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
        
        //get current user orders - in real app, fetch from backend

        List<OrderDTO> ordersDTOs = ApiService.getUserOrders(getLoggedInUser().getId());
        for (OrderDTO dto : ordersDTOs) {
            orders.add(new Order(
                dto.getId(),
                dto.getOrderDate().toString(),
                dto.getDeliveryDate().toString(),
                dto.getRecipientName()+ " (" + dto.getRecipientPhone() + ")",
                dto.getTotalPrice(),
                dto.getStatus()
            ));
        }
        
        
        ordersTable.setItems(orders);
    }
    
    @FXML
    private void handleBackToCatalog() {
        navigateToWithSize("/com/lilach/client/views/catalog.fxml", "Lilach Flower Shop Catalog", 1200, 800);
    }

    @FXML
    private void handleLogout() {
        logout();
    }

    @FXML
    private void handleCancelOrder() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null && selectedOrder.getStatus().equals("Processing")) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Cancel Order");
            confirm.setHeaderText("Cancel Order #" + selectedOrder.getId());
            confirm.setContentText("Are you sure you want to cancel this order?");
            
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    showSuccess("Order Cancelled", "Order #" + selectedOrder.getId() + " has been cancelled.");
                    // In real app, update status in backend

                    orders.remove(selectedOrder);
                }
            });
        } else {
            showError("Error", "You can only cancel orders that are still processing.");
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
        public String getStatus() { 
            return status.get(); 
        }
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
                        String status =order.getStatus();
                        cancelBtn.setDisable(status.equals( "CANCELLED" )|| LocalDateTime.parse(order.getOrderDate()).isBefore(LocalDateTime.now().minusDays(1)));
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



    // Add a new tab or button for complaints management
    @FXML
    private void handleViewComplaints() {
        navigateTo("/com/lilach/client/views/complaints_manager.fxml", "Complaints Management");
    }

    @FXML
    private void handleCreateComplaint() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showError("Selection Error", "Please select an order to create a complaint");
            return;
        }
        
        navigateTo("/com/lilach/client/views/complaint.fxml", "Submit Complaint");
    }
    
    private void cancelOrder(Order order) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Order");
        confirm.setHeaderText("Cancel Order #" + order.getId());
        confirm.setContentText("Are you sure you want to cancel this order?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    ApiService.cancelOrder(order.getId());
                    orders.remove(order);
                } catch (IOException e) {
                    System.out.println("Failed to cancel order: " + e.getMessage());
                    e.printStackTrace();
                }
                showSuccess("Order Cancelled", "Order #" + order.getId() + " has been cancelled.");
            }
        });
    }
    
 
}