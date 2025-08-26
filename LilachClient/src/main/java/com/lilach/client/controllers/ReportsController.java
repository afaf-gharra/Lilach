package com.lilach.client.controllers;

import com.lilach.client.models.ReportDTO;
import com.lilach.client.services.ApiService;
import com.lilach.client.utils.ReportPdfGenerator;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ReportsController extends BaseController {
    
    @FXML private ComboBox<String> reportTypeCombo;
    @FXML private ComboBox<Integer> yearCombo;
    @FXML private ComboBox<String> quarterCombo;
    @FXML private Label reportTitle;
    @FXML private Label totalRevenue;
    @FXML private Label totalProfit;
    @FXML private Label avgOrderValue;
    @FXML private Label totalOrders;
    @FXML private Label completedOrders;
    @FXML private Label completionRate;
    @FXML private Label totalComplaints;
    @FXML private Label resolvedComplaints;
    @FXML private Label resolutionRate;
    @FXML private ListView<String> topProductsList;
    @FXML private Button exportButton;
    
    private ReportDTO currentReport;
    
    @FXML
    public void initialize() {
        setupReportTypeListener();
        loadAvailableYears();
    }
    
    private void setupReportTypeListener() {
        quarterCombo.getItems().setAll("Q1 (Jan-Mar)", "Q2 (Apr-Jun)", "Q3 (Jul-Sep)", "Q4 (Oct-Dec)");
        reportTypeCombo.getItems().setAll("Yearly Report", "Quarterly Report");
        
        reportTypeCombo.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldValue, newValue) -> {
                boolean isQuarterly = "Quarterly Report".equals(newValue);
                quarterCombo.setDisable(!isQuarterly);
                if (!isQuarterly) {
                    quarterCombo.getSelectionModel().clearSelection();
                }
            }
        );
    }
    
    private void loadAvailableYears() {
        Integer storeId = getLoggedInUser().getStoreId();
        if (storeId == null) {
            showError("Store Error", "No store assigned");
            return;
        }
        
        try {
            List<Integer> years = ApiService.getAvailableYears(storeId);
            yearCombo.getItems().setAll(years);
            if (!years.isEmpty()) {
                yearCombo.getSelectionModel().selectFirst();
            }
        } catch (IOException e) {
            showError("Connection Error", "Failed to load available years: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleGenerateReport() {
        Integer storeId = getLoggedInUser().getStoreId();
        if (storeId == null) {
            showError("Store Error", "No store assigned");
            return;
        }
        
        Integer selectedYear = yearCombo.getValue();
        if (selectedYear == null) {
            showError("Validation Error", "Please select a year");
            return;
        }
        
        String reportType = reportTypeCombo.getValue();
        if (reportType == null) {
            showError("Validation Error", "Please select a report type");
            return;
        }
        
        try {
            if ("Yearly Report".equals(reportType)) {
                currentReport = ApiService.getYearlyReport(storeId, selectedYear);
                displayReport(currentReport, "Yearly Report " + selectedYear);
            } else if ("Quarterly Report".equals(reportType)) {
                String quarter = quarterCombo.getValue();
                if (quarter == null) {
                    showError("Validation Error", "Please select a quarter");
                    return;
                }
                int quarterNum = quarterCombo.getSelectionModel().getSelectedIndex() + 1;
                currentReport = ApiService.getQuarterlyReport(storeId, selectedYear, quarterNum);
                displayReport(currentReport, "Q" + quarterNum + " " + selectedYear + " Report");
            }
            
            exportButton.setDisable(false);
            
        } catch (IOException e) {
            showError("Connection Error", "Failed to generate report: " + e.getMessage());
        }
    }
    
    private void displayReport(ReportDTO report, String title) {
        reportTitle.setText(title);
        
        // Financial metrics
        totalRevenue.setText(String.format("Total Revenue: $%.2f", report.getTotalRevenue()));
        totalProfit.setText(String.format("Total Profit: $%.2f", report.getProfit()));
        avgOrderValue.setText(String.format("Avg Order Value: $%.2f", report.getAverageOrderValue()));
        
        // Order metrics
        totalOrders.setText(String.format("Total Orders: %d", report.getTotalOrdersCount()));
        completedOrders.setText(String.format("Completed Orders: %d", report.getCompletedOrders()));
        completionRate.setText(String.format("Completion Rate: %.1f%%", report.getCompletionRate()));
        
        // Complaint metrics
        totalComplaints.setText(String.format("Total Complaints: %d", report.getTotalComplaints()));
        resolvedComplaints.setText(String.format("Resolved: %d", report.getResolvedComplaints()));
        resolutionRate.setText(String.format("Resolution Rate: %.1f%%", report.getResolutionRate()));
        
        // Top products
        if (report.getTopProducts() != null) {
            List<String> topProducts = report.getTopProducts().entrySet().stream()
                .map(entry -> String.format("%s: %d units", entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
            topProductsList.setItems(FXCollections.observableArrayList(topProducts));
        }
    }
    
            
    // In your controller
    @FXML
    private void handleExport() {
        if (currentReport != null) {
            ReportPdfGenerator.generateAndDownloadPdf(currentReport);
            showSuccess("Export Successful", "Report exported as PDF successfully.");
        }
        else {
            showError("Export Error", "No report to export. Please generate a report first.");
        }

    }

  
}