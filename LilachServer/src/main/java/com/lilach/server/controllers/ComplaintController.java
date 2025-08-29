package com.lilach.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lilach.server.models.Complaint;
import com.lilach.server.models.Order;
import com.lilach.server.models.User;
import com.lilach.server.services.ComplaintService;
import com.lilach.server.services.OrderService;
import com.lilach.server.services.UserService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

public class ComplaintController {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static void registerRoutes(io.javalin.Javalin app) {
        app.get("/api/complaints", ComplaintController::getAllComplaints);
        app.get("/api/complaints/{id}", ComplaintController::getComplaintById);
        app.get("/api/stores/{storeId}/complaints", ComplaintController::getComplaintsByStore);
        app.get("/api/users/{userId}/complaints", ComplaintController::getComplaintsByUser);
        app.get("/api/complaints/status/{status}", ComplaintController::getComplaintsByStatus);
        app.post("/api/complaints", ComplaintController::createComplaint);
        app.put("/api/complaints/{id}", ComplaintController::updateComplaint);
        app.delete("/api/complaints/{id}", ComplaintController::deleteComplaint);
        app.get("/api/stores/{storeId}/complaints/count", ComplaintController::getOpenComplaintsCount);
    }
    
    public static void getAllComplaints(Context ctx) {
        try {
            List<Complaint> complaints = ComplaintService.getAllComplaints();
            List<ComplaintDTO> complaintDTOs = complaints.stream()
                .map(ComplaintController::convertToDTO)
                .collect(Collectors.toList());
            ctx.json(complaintDTOs).status(HttpStatus.OK);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error retrieving complaints: " + e.getMessage());
        }
    }
    
    public static void getComplaintById(Context ctx) {
        try {
            int complaintId = Integer.parseInt(ctx.pathParam("id"));
            Complaint complaint = ComplaintService.getComplaintById(complaintId);
            
            if (complaint != null) {
                ctx.json(convertToDTO(complaint)).status(HttpStatus.OK);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json("Complaint not found");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error retrieving complaint: " + e.getMessage());
        }
    }
    
    public static void getComplaintsByStore(Context ctx) {
        try {
            int storeId = Integer.parseInt(ctx.pathParam("storeId"));
            List<Complaint> complaints = ComplaintService.getComplaintsByStore(storeId);
            List<ComplaintDTO> complaintDTOs = complaints.stream()
                .map(ComplaintController::convertToDTO)
                .collect(Collectors.toList());
            ctx.json(complaintDTOs).status(HttpStatus.OK);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error retrieving store complaints: " + e.getMessage());
        }
    }
    
    public static void getComplaintsByUser(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            List<Complaint> complaints = ComplaintService.getComplaintsByUser(userId);
            List<ComplaintDTO> complaintDTOs = complaints.stream()
                .map(ComplaintController::convertToDTO)
                .collect(Collectors.toList());
            ctx.json(complaintDTOs).status(HttpStatus.OK);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error retrieving user complaints: " + e.getMessage());
        }
    }
    
    public static void getComplaintsByStatus(Context ctx) {
        try {
            String statusStr = ctx.pathParam("status").toUpperCase();
            Complaint.ComplaintStatus status = Complaint.ComplaintStatus.valueOf(statusStr);
            
            List<Complaint> complaints = ComplaintService.getComplaintsByStatus(status);
            List<ComplaintDTO> complaintDTOs = complaints.stream()
                .map(ComplaintController::convertToDTO)
                .collect(Collectors.toList());
            ctx.json(complaintDTOs).status(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json("Invalid status value. Valid values: OPEN, IN_PROGRESS, RESOLVED, CLOSED");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error retrieving complaints by status: " + e.getMessage());
        }
    }
    
    public static void createComplaint(Context ctx) {
        try {
            mapper.registerModule(new JavaTimeModule());
            ComplaintDTO complaintDTO = mapper.readValue(ctx.body(), ComplaintDTO.class);
            
            // Validate required fields
            if (complaintDTO.getOrderId() == 0) {
                ctx.status(HttpStatus.BAD_REQUEST).json("Order ID is required");
                return;
            }
            if (complaintDTO.getUserId() == 0) {
                ctx.status(HttpStatus.BAD_REQUEST).json("User ID is required");
                return;
            }
            if (complaintDTO.getDescription() == null || complaintDTO.getDescription().trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST).json("Description is required");
                return;
            }
            
            // Get order and user
            Order order = OrderService.getOrderById(complaintDTO.getOrderId());
            User user = UserService.getUserById(complaintDTO.getUserId());
            
            if (order == null) {
                ctx.status(HttpStatus.NOT_FOUND).json("Order not found");
                return;
            }
            if (user == null) {
                ctx.status(HttpStatus.NOT_FOUND).json("User not found");
                return;
            }
            
            // Create complaint
            Complaint complaint = new Complaint(order, user, complaintDTO.getType(), complaintDTO.getDescription());
            complaint.setDesiredResolution(complaintDTO.getDesiredResolution());
            complaint.setContactEmail(complaintDTO.isContactEmail());
            complaint.setContactPhone(complaintDTO.isContactPhone());
            
            Complaint createdComplaint = ComplaintService.createComplaint(complaint);
            ctx.json(convertToDTO(createdComplaint)).status(HttpStatus.CREATED);
            
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error creating complaint: " + e.getMessage());
        }
    }
    
    public static void updateComplaint(Context ctx) {
        try {
            int complaintId = Integer.parseInt(ctx.pathParam("id"));
            mapper.registerModule(new JavaTimeModule());
            ComplaintDTO complaintDTO = mapper.readValue(ctx.body(), ComplaintDTO.class);
            
            Complaint complaintUpdates = new Complaint();
            complaintUpdates.setType(complaintDTO.getType());
            complaintUpdates.setDescription(complaintDTO.getDescription());
            complaintUpdates.setDesiredResolution(complaintDTO.getDesiredResolution());
            complaintUpdates.setResolutionNotes(complaintDTO.getResolutionNotes());
            complaintUpdates.setCompensation(complaintDTO.getCompensation());
            complaintUpdates.setContactEmail(complaintDTO.isContactEmail());
            complaintUpdates.setContactPhone(complaintDTO.isContactPhone());
            
            if (complaintDTO.getStatus() != null) {
                try {
                    Complaint.ComplaintStatus status = Complaint.ComplaintStatus.valueOf(complaintDTO.getStatus().toUpperCase());
                    complaintUpdates.setStatus(status);
                } catch (IllegalArgumentException e) {
                    ctx.status(HttpStatus.BAD_REQUEST).json("Invalid status value");
                    return;
                }
            }
            
            Complaint updatedComplaint = ComplaintService.updateComplaint(complaintId, complaintUpdates);
            if (updatedComplaint != null) {
                ctx.json(convertToDTO(updatedComplaint)).status(HttpStatus.OK);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json("Complaint not found");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error updating complaint: " + e.getMessage());
        }
    }
    
    public static void deleteComplaint(Context ctx) {
        try {
            int complaintId = Integer.parseInt(ctx.pathParam("id"));
            boolean deleted = ComplaintService.deleteComplaint(complaintId);
            
            if (deleted) {
                ctx.status(HttpStatus.NO_CONTENT);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json("Complaint not found");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error deleting complaint: " + e.getMessage());
        }
    }
    
    public static void getOpenComplaintsCount(Context ctx) {
        try {
            int storeId = Integer.parseInt(ctx.pathParam("storeId"));
            long count = ComplaintService.getOpenComplaintsCount(storeId);
            ctx.json(new ComplaintCountDTO(storeId, count)).status(HttpStatus.OK);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error retrieving complaints count: " + e.getMessage());
        }
    }
    
    // DTO conversion
    private static ComplaintDTO convertToDTO(Complaint complaint) {
        ComplaintDTO dto = new ComplaintDTO();
        dto.setId(complaint.getId());
        dto.setOrderId(complaint.getOrder().getId());
        dto.setUserId(complaint.getUser().getId());
        dto.setCustomerName(complaint.getUser().getFullName());
        dto.setType(complaint.getType());
        dto.setDescription(complaint.getDescription());
        dto.setDesiredResolution(complaint.getDesiredResolution());
        dto.setResolutionNotes(complaint.getResolutionNotes());
        dto.setCompensation(complaint.getCompensation() != null ? complaint.getCompensation() : 0.0);
        dto.setStatus(complaint.getStatus().toString());
        dto.setContactEmail(complaint.isContactEmail());
        dto.setContactPhone(complaint.isContactPhone());
        dto.setCreatedAt(complaint.getCreatedAt());
        dto.setResolvedAt(complaint.getResolvedAt());
        return dto;
    }
    
    // Inner DTO classes
    public static class ComplaintDTO {
        private int id;
        private int orderId;
        private int userId;
        private String customerName;
        private String type;
        private String description;
        private String desiredResolution;
        private String resolutionNotes;
        private double compensation;
        private String status;
        private boolean contactEmail;
        private boolean contactPhone;
        private java.time.LocalDateTime createdAt;
        private java.time.LocalDateTime resolvedAt;
        
        // Getters and setters
        public int getId() { return id; } public void setId(int id) { this.id = id; }
        public int getOrderId() { return orderId; } public void setOrderId(int orderId) { this.orderId = orderId; }
        public int getUserId() { return userId; } public void setUserId(int userId) { this.userId = userId; }
        public String getCustomerName() { return customerName; } public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getType() { return type; } public void setType(String type) { this.type = type; }
        public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
        public String getDesiredResolution() { return desiredResolution; } public void setDesiredResolution(String desiredResolution) { this.desiredResolution = desiredResolution; }
        public String getResolutionNotes() { return resolutionNotes; } public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }
        public double getCompensation() { return compensation; } public void setCompensation(double compensation) { this.compensation = compensation; }
        public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
        public boolean isContactEmail() { return contactEmail; } public void setContactEmail(boolean contactEmail) { this.contactEmail = contactEmail; }
        public boolean isContactPhone() { return contactPhone; } public void setContactPhone(boolean contactPhone) { this.contactPhone = contactPhone; }
        public java.time.LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }
        public java.time.LocalDateTime getResolvedAt() { return resolvedAt; } public void setResolvedAt(java.time.LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    }
    
    public static class ComplaintCountDTO {
        private int storeId;
        private long openComplaintsCount;
        
        public ComplaintCountDTO(int storeId, long openComplaintsCount) {
            this.storeId = storeId;
            this.openComplaintsCount = openComplaintsCount;
        }
        
        // Getters
        public int getStoreId() { return storeId; }
        public long getOpenComplaintsCount() { return openComplaintsCount; }
    }
}