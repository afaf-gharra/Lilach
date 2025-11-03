package com.lilach.server.controllers;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lilach.server.models.Refund;
import com.lilach.server.services.RefundService;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class RefundController {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static void registerRoutes(io.javalin.Javalin app) {
        app.post("/api/orders/{id}/cancel", RefundController::cancelOrderWithRefund);
        app.get("/api/orders/{id}/refund", RefundController::getRefundByOrder);
        app.get("/api/users/{userId}/refunds", RefundController::getUserRefunds);
    }
    
    public static void cancelOrderWithRefund(Context ctx) {
        try {
            int orderId = Integer.parseInt(ctx.pathParam("id"));
            CancelRequest cancelRequest = mapper.readValue(ctx.body(), CancelRequest.class);
            
            Refund refund = RefundService.calculateAndCreateRefund(orderId, cancelRequest.getReason());
            RefundDTO refundDTO = convertToDTO(refund);
            
            ctx.json(refundDTO).status(HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json(e.getMessage());
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error cancelling order: " + e.getMessage());
        }
    }
    
    public static void getRefundByOrder(Context ctx) {
        try {
            int orderId = Integer.parseInt(ctx.pathParam("id"));
            Refund refund = RefundService.getRefundByOrderId(orderId);
            
            if (refund != null) {
                ctx.json(convertToDTO(refund)).status(HttpStatus.OK);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json("Refund not found for this order");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error retrieving refund: " + e.getMessage());
        }
    }
    
    public static void getUserRefunds(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            List<Refund> refunds = RefundService.getRefundsByUser(userId);
            List<RefundDTO> refundDTOs = refunds.stream()
                .map(RefundController::convertToDTO)
                .collect(Collectors.toList());
            ctx.json(refundDTOs).status(HttpStatus.OK);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error retrieving user refunds: " + e.getMessage());
        }
    }
    
    // DTO conversion
    private static RefundDTO convertToDTO(Refund refund) {
        RefundDTO dto = new RefundDTO();
        dto.setId(refund.getId());
        dto.setOrderId(refund.getOrder().getId());
        dto.setRefundAmount(refund.getRefundAmount());
        dto.setRefundPercentage(refund.getRefundPercentage());
        dto.setCancellationReason(refund.getCancellationReason());
        dto.setRefundStatus(refund.getRefundStatus().toString());
        dto.setCancelledAt(refund.getCancelledAt());
        return dto;
    }
    
    // Request DTO
    public static class CancelRequest {
        private String reason;
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    // Response DTO
    public static class RefundDTO {
        private int id;
        private int orderId;
        private double refundAmount;
        private int refundPercentage;
        private String cancellationReason;
        private String refundStatus;
        private java.time.LocalDateTime cancelledAt;
        
        // Getters and setters
        public int getId() { return id; } public void setId(int id) { this.id = id; }
        public int getOrderId() { return orderId; } public void setOrderId(int orderId) { this.orderId = orderId; }
        public double getRefundAmount() { return refundAmount; } public void setRefundAmount(double refundAmount) { this.refundAmount = refundAmount; }
        public int getRefundPercentage() { return refundPercentage; } public void setRefundPercentage(int refundPercentage) { this.refundPercentage = refundPercentage; }
        public String getCancellationReason() { return cancellationReason; } public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
        public String getRefundStatus() { return refundStatus; } public void setRefundStatus(String refundStatus) { this.refundStatus = refundStatus; }
        public java.time.LocalDateTime getCancelledAt() { return cancelledAt; } public void setCancelledAt(java.time.LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    }
}