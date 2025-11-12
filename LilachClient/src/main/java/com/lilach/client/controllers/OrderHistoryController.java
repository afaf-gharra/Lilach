package com.lilach.client.controllers;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.lilach.client.models.OrderDTO;
import com.lilach.client.models.RefundDTO;
import com.lilach.client.services.ApiService;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    private void handleComplaints() {
        navigateTo("/com/lilach/client/views/complaints.fxml", "Complaints Form");
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
                        String status = order.getStatus();

                        boolean isCancelled = "CANCELLED".equalsIgnoreCase(status);
                        boolean isDelivered = "DELIVERED".equalsIgnoreCase(status);
                        boolean tooOld = false;
                        try {
                            tooOld = LocalDateTime.parse(order.getOrderDate())
                                                .isBefore(LocalDateTime.now().minusDays(1));
                        } catch (Exception ignore) { /* keep safe if format changes */ }

                        // disable when: cancelled OR delivered OR past allowed time
                        cancelBtn.setDisable(isCancelled || isDelivered || tooOld);
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
        // Create cancellation dialog with reason input
        Dialog<CancelResult> dialog = new Dialog<>();
        dialog.setTitle("Cancel Order");
        dialog.setHeaderText("Cancel Order #" + order.getId());
        
        // Set the button types
        ButtonType cancelButtonType = new ButtonType("Cancel Order", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(cancelButtonType, ButtonType.CANCEL);
        
        // Create the reason input
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label infoLabel = new Label("Cancellation refund policy:\n" +
            "• More than 3 hours before delivery: 100% refund\n" +
            "• 1-3 hours before delivery: 50% refund\n" +
            "• Less than 1 hour before delivery: 0% refund");
        infoLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        
        TextArea reasonField = new TextArea();
        reasonField.setPromptText("Please provide a reason for cancellation (optional)");
        reasonField.setPrefHeight(80);
        
        content.getChildren().addAll(infoLabel, new Label("Cancellation Reason:"), reasonField);
        dialog.getDialogPane().setContent(content);
        
        // Enable/disable cancel button based on input
        Node cancelButton = dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.setDisable(false);
        
        // Convert the result to CancelResult when the cancel button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == cancelButtonType) {
                return new CancelResult(reasonField.getText().trim());
            }
            return null;
        });
    
    // Show dialog and process cancellation
        Optional<CancelResult> result = dialog.showAndWait();
        result.ifPresent(cancelResult -> {
            try {
                RefundDTO refund = ApiService.cancelOrderWithRefund(order.getId(), cancelResult.getReason());
                
                if (refund != null) {
                    // Show refund information to user
                    showRefundInformation(refund, order);
                    
                    // Remove order from list
                    orders.remove(order);
                    ordersTable.refresh();
                } else {
                    showError("Cancellation Failed", "Failed to cancel the order. Please try again.");
                }
            } catch (IOException e) {
                showError("Connection Error", "Failed to cancel order: " + e.getMessage());
            }
        });
    }

    private void showRefundInformation(RefundDTO refund, Order order) {
        String message;
        
        if (refund.getRefundPercentage() == 0) {
            message = String.format(
                "Order #%d has been cancelled.\n\n" +
                "Refund Amount: $%.2f (%d%%)\n" +
                "Note: No refund is provided for cancellations made less than 1 hour before delivery.",
                order.getId(), refund.getRefundAmount(), refund.getRefundPercentage()
            );
        } else {
            message = String.format(
                "Order #%d has been cancelled successfully!\n\n" +
                "Refund Amount: $%.2f (%d%% of order total)\n" +
                "The refund will be processed to your original payment method within 3-5 business days.",
                order.getId(), refund.getRefundAmount(), refund.getRefundPercentage()
            );
        }
        
        if (refund.getCancellationReason() != null && !refund.getCancellationReason().isEmpty()) {
            message += "\n\nReason: " + refund.getCancellationReason();
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Cancelled");
        alert.setHeaderText("Cancellation Complete");
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Helper class for cancellation result
    private static class CancelResult {
        private final String reason;
        
        public CancelResult(String reason) {
            this.reason = reason;
        }
        
        public String getReason() {
            return reason;
        }
    }
    
 
}