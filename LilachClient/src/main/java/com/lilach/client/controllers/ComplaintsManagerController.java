package com.lilach.client.controllers;

import com.lilach.client.models.ComplaintDTO;
import com.lilach.client.services.ApiService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

public class ComplaintsManagerController extends BaseController {
    
    @FXML private TableView<ComplaintDTO> complaintsTable;
    @FXML private TableColumn<ComplaintDTO, Integer> idColumn;
    @FXML private TableColumn<ComplaintDTO, Integer> orderIdColumn;
    @FXML private TableColumn<ComplaintDTO, String> customerColumn;
    @FXML private TableColumn<ComplaintDTO, String> typeColumn;
    @FXML private TableColumn<ComplaintDTO, String> statusColumn;
    @FXML private TableColumn<ComplaintDTO, String> createdColumn;
    @FXML private TableColumn<ComplaintDTO, Void> actionsColumn;
    
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TextArea complaintDetails;
    @FXML private TextArea resolutionNotes;
    @FXML private TextField compensationField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea customerInfo;
    @FXML private TextArea orderDetails;
    @FXML private TextArea contactInfo;
    @FXML private Button resolveButton;
    @FXML private Button refreshButton;
    
    private ObservableList<ComplaintDTO> complaints = FXCollections.observableArrayList();
    private ComplaintDTO selectedComplaint;
    
    @FXML
    public void initialize() {
        setupTable();
        setupFilters();
        setupForm();
        loadComplaints();
    }
    
    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdColumn.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue().getCreatedAt() != null ?
                cellData.getValue().getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")) : ""
            )
        );
        
        // Actions column with button to view details
        actionsColumn.setCellFactory(param -> new TableCell<ComplaintDTO, Void>() {
            private final Button viewButton = new Button("View Details");
            
            {
                viewButton.getStyleClass().add("btn-info");
                viewButton.setOnAction(event -> {
                    ComplaintDTO complaint = getTableView().getItems().get(getIndex());
                    showComplaintDetails(complaint);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(viewButton);
                }
            }
        });
        
        complaintsTable.setItems(complaints);
        complaintsTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> showComplaintDetails(newSelection));
    }
    
    private void setupFilters() {
        statusFilterCombo.getItems().addAll("ALL", "OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED");
        statusFilterCombo.setValue("OPEN");
        statusFilterCombo.setOnAction(e -> filterComplaints());
    }
    
    private void setupForm() {
        statusCombo.getItems().addAll("OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED");
        
        // Numeric validation for compensation
        compensationField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                compensationField.setText(oldValue);
            }
        });
        
        resolveButton.setOnAction(e -> resolveComplaint());
    }
    
    private void loadComplaints() {
        Integer storeId = LoginController.loggedInUser.getStoreId();

        try {
            // Load complaints for the manager's store
            var storeComplaints = ApiService.getStoreComplaints(storeId);
            complaints.setAll(storeComplaints);
            filterComplaints();
            
        } catch (IOException e) {
            showError("Connection Error", "Failed to load complaints: " + e.getMessage());
        }
    }
    
    private void filterComplaints() {
        String filter = statusFilterCombo.getValue();
        if ("ALL".equals(filter)) {
            complaintsTable.setItems(complaints);
        } else {
            ObservableList<ComplaintDTO> filtered = complaints.stream()
                .filter(complaint -> filter.equals(complaint.getStatus()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
            complaintsTable.setItems(filtered);
        }
    }
    
    private void showComplaintDetails(ComplaintDTO complaint) {
        selectedComplaint = complaint;
        if (complaint == null) {
            clearForm();
            return;
        }
        
        // Fill complaint details
        complaintDetails.setText(complaint.getDescription());
        resolutionNotes.setText(complaint.getResolutionNotes() != null ? complaint.getResolutionNotes() : "");
        compensationField.setText(complaint.getCompensation() > 0 ? String.valueOf(complaint.getCompensation()) : "");
        statusCombo.setValue(complaint.getStatus());
        
        // Load additional information
        loadCustomerInfo(complaint);
        loadOrderInfo(complaint);
        
        resolveButton.setDisable(false);
    }
    
    private void loadCustomerInfo(ComplaintDTO complaint) {
        try {
            // Load customer information
            var customer = ApiService.getUserById(complaint.getUserId());
            if (customer != null) {
                customerInfo.setText(String.format(
                    "Name: %s\nEmail: %s\nPhone: %s",
                    customer.getFullName(), customer.getEmail(), customer.getPhone()
                ));
                
                contactInfo.setText(String.format(
                    "Contact via: %s%s",
                    complaint.isContactEmail() ? "Email" : "",
                    complaint.isContactPhone() ? (complaint.isContactEmail() ? " + Phone" : "Phone") : ""
                ));
            }
        } catch (IOException e) {
            customerInfo.setText("Error loading customer information");
        }
    }
    
    private void loadOrderInfo(ComplaintDTO complaint) {
        try {
            // Load order information
            var order = ApiService.getOrderById(complaint.getOrderId());
            if (order != null) {
                orderDetails.setText(String.format(
                    "Order #: %d\nDate: %s\nTotal: $%.2f\nStatus: %s",
                    order.getId(),
                    order.getOrderDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                    order.getTotalPrice(),
                    order.getStatus()
                ));
            }
        } catch (IOException e) {
            orderDetails.setText("Error loading order information");
        }
    }
    
    private void resolveComplaint() {
        if (selectedComplaint == null) {
            showError("Selection Error", "Please select a complaint to resolve");
            return;
        }
        
        if (resolutionNotes.getText().trim().isEmpty()) {
            showError("Validation Error", "Please enter resolution notes");
            return;
        }
        
        try {
            selectedComplaint.setResolutionNotes(resolutionNotes.getText().trim());
            selectedComplaint.setStatus(statusCombo.getValue());
            
            if (!compensationField.getText().isEmpty()) {
                selectedComplaint.setCompensation(Double.parseDouble(compensationField.getText()));
            }
            
            ComplaintDTO resolvedComplaint = ApiService.updateComplaint(selectedComplaint);
            if (resolvedComplaint != null) {
                // Update local data
                int index = complaints.indexOf(selectedComplaint);
                if (index >= 0) {
                    complaints.set(index, resolvedComplaint);
                    complaintsTable.refresh();
                }
                showSuccess("Complaint Updated", "Complaint status updated to: " + resolvedComplaint.getStatus());
            } else {
                showError("Update Failed", "Failed to update complaint");
            }
        } catch (NumberFormatException e) {
            showError("Validation Error", "Please enter a valid compensation amount");
        } catch (IOException e) {
            showError("Connection Error", "Failed to update complaint: " + e.getMessage());
        }
    }
    
    private void clearForm() {
        complaintDetails.clear();
        resolutionNotes.clear();
        compensationField.clear();
        customerInfo.clear();
        orderDetails.clear();
        contactInfo.clear();
        statusCombo.setValue("OPEN");
        resolveButton.setDisable(true);
    }
    
    @FXML
    private void handleRefresh() {
        loadComplaints();
        showSuccess("Refreshed", "Complaints list refreshed");
    }
}