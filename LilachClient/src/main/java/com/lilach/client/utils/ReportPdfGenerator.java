package com.lilach.client.utils;

import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.lilach.client.models.ReportDTO;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

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
        Paragraph title = new Paragraph("LILACH FLOWER SHOP - STORE REPORT")
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(20)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
            .setFontColor(new DeviceRgb(106, 13, 173)); // Purple color
        document.add(title);
        
        // Report period
        String period = report.getPeriodType() + " Report: " +
            report.getStartDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy")) + " to " +
            report.getEndDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
        document.add(new Paragraph(period)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(12)
            .setMarginBottom(20)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE)));
        
        // Financial Summary
        document.add(new Paragraph("FINANCIAL SUMMARY")
            .setFontSize(14)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
            .setMarginTop(15)
            .setFontColor(new DeviceRgb(106, 13, 173)));
        
        Table financialTable = createStyledTable(2);
        addHeaderCell(financialTable, "Metric");
        addHeaderCell(financialTable, "Value");
        financialTable.addCell(createDataCell("Total Revenue:")); 
        financialTable.addCell(createDataCell(String.format("$%.2f", report.getTotalRevenue())));
        financialTable.addCell(createDataCell("Total Profit:")); 
        financialTable.addCell(createDataCell(String.format("$%.2f", report.getProfit())));
        financialTable.addCell(createDataCell("Average Order Value:")); 
        financialTable.addCell(createDataCell(String.format("$%.2f", report.getAverageOrderValue())));
        document.add(financialTable);
        
        // Order Summary
        document.add(new Paragraph("ORDER SUMMARY")
            .setFontSize(14)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
            .setMarginTop(15)
            .setFontColor(new DeviceRgb(106, 13, 173)));
        
        Table orderTable = createStyledTable(2);
        addHeaderCell(orderTable, "Metric");
        addHeaderCell(orderTable, "Value");
        orderTable.addCell(createDataCell("Total Orders:")); 
        orderTable.addCell(createDataCell(String.valueOf(report.getTotalOrdersCount())));
        orderTable.addCell(createDataCell("Completed Orders:")); 
        orderTable.addCell(createDataCell(String.valueOf(report.getCompletedOrders())));
        orderTable.addCell(createDataCell("Completion Rate:")); 
        orderTable.addCell(createDataCell(String.format("%.1f%%", report.getCompletionRate())));
        document.add(orderTable);
        
        // Sales By Category
        if (report.getCategories() != null && !report.getCategories().isEmpty()) {
            document.add(new Paragraph("SALES BY CATEGORY")
                .setFontSize(14)
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setMarginTop(15)
                .setFontColor(new DeviceRgb(106, 13, 173)));
            
            Table categoryTable = createStyledTable(2);
            addHeaderCell(categoryTable, "Category");
            addHeaderCell(categoryTable, "Orders");
            
            report.getCategories().forEach((category, count) -> {
                categoryTable.addCell(createDataCell(category));
                categoryTable.addCell(createDataCell(String.valueOf(count)));
            });
            document.add(categoryTable);
        }
        
        // Complaint Summary
        document.add(new Paragraph("COMPLAINT SUMMARY")
            .setFontSize(14)
            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
            .setMarginTop(15)
            .setFontColor(new DeviceRgb(106, 13, 173)));
        
        Table complaintTable = createStyledTable(2);
        addHeaderCell(complaintTable, "Metric");
        addHeaderCell(complaintTable, "Value");
        complaintTable.addCell(createDataCell("Total Complaints:")); 
        complaintTable.addCell(createDataCell(String.valueOf(report.getTotalComplaints())));
        complaintTable.addCell(createDataCell("Pending:")); 
        complaintTable.addCell(createDataCell(String.valueOf(report.getPendingComplaints())));
        complaintTable.addCell(createDataCell("Resolved:")); 
        complaintTable.addCell(createDataCell(String.valueOf(report.getResolvedComplaints())));
        complaintTable.addCell(createDataCell("Resolution Rate:")); 
        complaintTable.addCell(createDataCell(String.format("%.1f%%", report.getResolutionRate())));
        document.add(complaintTable);
        
        // Pending Complaint Customers
        if (report.getPendingComplaintCustomers() != null && !report.getPendingComplaintCustomers().isEmpty()) {
            document.add(new Paragraph("CUSTOMERS WITH PENDING COMPLAINTS")
                .setFontSize(12)
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setMarginTop(10)
                .setFontColor(new DeviceRgb(211, 84, 0))); // Orange color for attention
            
            for (String customerName : report.getPendingComplaintCustomers()) {
                document.add(new Paragraph("• " + customerName)
                    .setFontSize(10)
                    .setMarginLeft(20));
            }
        }
        
        // Top Products
        if (report.getTopProducts() != null && !report.getTopProducts().isEmpty()) {
            document.add(new Paragraph("TOP PRODUCTS")
                .setFontSize(14)
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setMarginTop(15)
                .setFontColor(new DeviceRgb(106, 13, 173)));
            
            Table productsTable = createStyledTable(2);
            addHeaderCell(productsTable, "Product Name");
            addHeaderCell(productsTable, "Quantity Sold");
            
            report.getTopProducts().forEach((name, quantity) -> {
                productsTable.addCell(createDataCell(name));
                productsTable.addCell(createDataCell(String.valueOf(quantity)));
            });
            document.add(productsTable);
        }
        
        document.close();
    }
    
    private static Table createStyledTable(int columns) {
        Table table = new Table(columns);
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(10);
        return table;
    }
    
    private static void addHeaderCell(Table table, String text) {
        try {
            Cell cell = new Cell()
                .add(new Paragraph(text)
                    .setFontSize(11)
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)))
                .setBackgroundColor(new DeviceRgb(106, 13, 173))
                .setFontColor(ColorConstants.WHITE)
                .setPadding(8)
                .setTextAlignment(TextAlignment.CENTER);
            table.addCell(cell);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create header cell", e);
        }
    }
    
    private static Cell createDataCell(String text) {
        return new Cell()
            .add(new Paragraph(text).setFontSize(10))
            .setPadding(6)
            .setBorder(new SolidBorder(new DeviceRgb(200, 200, 200), 1));
    }
}