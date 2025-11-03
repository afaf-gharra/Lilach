package com.lilach.client.controllers;

import java.io.IOException;

import com.lilach.client.models.RefundDTO;
import com.lilach.client.services.ApiService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class RefundHistoryController extends BaseController {
    
    @FXML private TableView<RefundDTO> refundsTable;
    @FXML private Label totalRefundsLabel;
    
    private ObservableList<RefundDTO> refunds = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        setupTable();
        loadRefunds();
    }
    
    private void setupTable() {
        TableColumn<RefundDTO, Integer> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        
        TableColumn<RefundDTO, Double> amountCol = new TableColumn<>("Refund Amount");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("refundAmount"));
        amountCol.setCellFactory(column -> new TableCell<RefundDTO, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                setText(empty || amount == null ? "" : String.format("$%.2f", amount));
            }
        });
        
        TableColumn<RefundDTO, Integer> percentageCol = new TableColumn<>("Percentage");
        percentageCol.setCellValueFactory(new PropertyValueFactory<>("refundPercentage"));
        percentageCol.setCellFactory(column -> new TableCell<RefundDTO, Integer>() {
            @Override
            protected void updateItem(Integer percentage, boolean empty) {
                super.updateItem(percentage, empty);
                setText(empty || percentage == null ? "" : percentage + "%");
            }
        });
        
        TableColumn<RefundDTO, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("refundStatus"));
        
        TableColumn<RefundDTO, String> dateCol = new TableColumn<>("Cancelled Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("cancelledAt"));
        
        TableColumn<RefundDTO, String> reasonCol = new TableColumn<>("Reason");
        reasonCol.setCellValueFactory(new PropertyValueFactory<>("cancellationReason"));
        
        refundsTable.getColumns().addAll(orderIdCol, amountCol, percentageCol, statusCol, dateCol, reasonCol);
        refundsTable.setItems(refunds);
    }
    
    private void loadRefunds() {
        Integer userId = getLoggedInUser().getId();

        try {
            refunds.setAll(ApiService.getUserRefunds(userId));
            calculateTotalRefunds();
        } catch (IOException e) {
            showError("Connection Error", "Failed to load refund history: " + e.getMessage());
        }
    }
    
    private void calculateTotalRefunds() {
        double total = refunds.stream()
            .mapToDouble(RefundDTO::getRefundAmount)
            .sum();
        totalRefundsLabel.setText(String.format("$%.2f", total));
    }
}