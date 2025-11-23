package com.lilach.client.controllers;

import com.lilach.client.services.CartItem;
import com.lilach.client.services.CartService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.scene.control.*; 
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;


public class CartController extends BaseController {
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
        
        // Load cart items from CartService
        cartItems = CartService.getInstance().getCartItems();
        

        cartTable.setItems(cartItems);
        calculateTotal();
    }
    

    private void calculateTotal() {
        total = cartItems.stream()
            .mapToDouble(item -> item.getPrice() * item.getQuantity())
            .sum();
        totalPriceLabel.setText(String.format("Total: $%.2f", total));
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
                        CartService.getInstance().removeItem(item.getId());
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

    @FXML
    private void handleContinueShopping() {
        navigateToWithSize("/com/lilach/client/views/catalog.fxml", "Lilach Flower Shop Catalog", 1200, 800);
    }

    @FXML
    private void handleCheckout() {
        // Ensure user is logged in before proceeding to checkout
        if (getLoggedInUser() == null) {
            // getLoggedInUser already shows error and redirects to login
            return;
        }
        navigateTo("/com/lilach/client/views/checkout.fxml", "Checkout");
    }

    @FXML
    private void handleLogout() {
        logout();
    }

    @FXML
    private void handleViewCart() {
        navigateTo("/com/lilach/client/views/cart.fxml", "Shopping Cart");
    }
    
    @FXML
    private void handleViewOrders() {
        navigateTo("/com/lilach/client/views/order_history.fxml", "My Orders");
    }

    @FXML
    private void handleComplaints() {
        navigateTo("/com/lilach/client/views/complaints.fxml", "Complaints Form");
    }

 
    
}