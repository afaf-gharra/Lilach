package com.lilach.client.utils;

import com.lilach.client.models.ReportDTO;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class ReportPdfGenerator {

    public static void generateAndDownloadPdf(ReportDTO report) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("lilach_report_" + 
                report.getStartDate().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            
            File file = fileChooser.showSaveDialog(new Stage());
            if (file != null) {
                generatePdf(report, file.getAbsolutePath());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
        }
    }

    private static void generatePdf(ReportDTO report, String filePath) throws IOException {
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        // Title
        document.add(new Paragraph("LILACH FLOWER SHOP - STORE REPORT")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(18));
        
        // Report period
        String period = report.getPeriodType() + " Report: " +
            report.getStartDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy")) + " to " +
            report.getEndDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        document.add(new Paragraph(period)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(12)
            .setMarginBottom(20));
        
        // Financial Summary
        document.add(new Paragraph("FINANCIAL SUMMARY")
            .setFontSize(14)
            .setMarginTop(15));
        
        Table financialTable = new Table(2);
        financialTable.setWidth(UnitValue.createPercentValue(100));
        financialTable.addCell("Total Revenue:"); financialTable.addCell(String.format("$%.2f", report.getTotalRevenue()));
        financialTable.addCell("Total Profit:"); financialTable.addCell(String.format("$%.2f", report.getProfit()));
        financialTable.addCell("Average Order Value:"); financialTable.addCell(String.format("$%.2f", report.getAverageOrderValue()));
        document.add(financialTable);
        
        // Order Summary
        document.add(new Paragraph("ORDER SUMMARY")
            .setFontSize(14)
            .setMarginTop(15));
        
        Table orderTable = new Table(2);
        orderTable.setWidth(UnitValue.createPercentValue(100));
        orderTable.addCell("Total Orders:"); orderTable.addCell(String.valueOf(report.getTotalOrdersCount()));
        orderTable.addCell("Completed Orders:"); orderTable.addCell(String.valueOf(report.getCompletedOrders()));
        orderTable.addCell("Completion Rate:"); orderTable.addCell(String.format("%.1f%%", report.getCompletionRate()));
        document.add(orderTable);
        
        // Complaint Summary
        document.add(new Paragraph("COMPLAINT SUMMARY")
            .setFontSize(14)
            .setMarginTop(15));
        
        Table complaintTable = new Table(2);
        complaintTable.setWidth(UnitValue.createPercentValue(100));
        complaintTable.addCell("Total Complaints:"); complaintTable.addCell(String.valueOf(report.getTotalComplaints()));
        complaintTable.addCell("Resolved Complaints:"); complaintTable.addCell(String.valueOf(report.getResolvedComplaints()));
        complaintTable.addCell("Resolution Rate:"); complaintTable.addCell(String.format("%.1f%%", report.getResolutionRate()));
        document.add(complaintTable);
        
        // Top Products
        if (report.getTopProducts() != null && !report.getTopProducts().isEmpty()) {
            document.add(new Paragraph("TOP PRODUCTS")
                .setFontSize(14)
                .setMarginTop(15));
            
            Table productsTable = new Table(2);
            productsTable.setWidth(UnitValue.createPercentValue(100));
            productsTable.addCell("Product Name"); productsTable.addCell("Quantity Sold");
            
            report.getTopProducts().forEach((name, quantity) -> {
                productsTable.addCell(name);
                productsTable.addCell(String.valueOf(quantity));
            });
            document.add(productsTable);
        }
        
        document.close();
    }
}