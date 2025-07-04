package com.lilach.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lilach.server.models.Complaint;
import com.lilach.server.services.ComplaintService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class ComplaintController {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static void registerRoutes(io.javalin.Javalin app) {
        app.post("/api/complaints", ComplaintController::createComplaint);
        app.put("/api/complaints/{id}/resolve", ComplaintController::resolveComplaint);
    }
    
    public static void createComplaint(Context ctx) {
        try {
            Complaint complaint = mapper.readValue(ctx.body(), Complaint.class);
            Complaint createdComplaint = ComplaintService.createComplaint(complaint);
            ctx.json(createdComplaint).status(HttpStatus.CREATED);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error creating complaint: " + e.getMessage());
        }
    }
    
    public static void resolveComplaint(Context ctx) {
        try {
            int complaintId = Integer.parseInt(ctx.pathParam("id"));
            double compensation = Double.parseDouble(ctx.queryParam("compensation"));
            Complaint resolvedComplaint = ComplaintService.resolveComplaint(complaintId, compensation);
            if (resolvedComplaint != null) {
                ctx.json(resolvedComplaint).status(HttpStatus.OK);
            } else {
                ctx.status(HttpStatus.BAD_REQUEST).json("Cannot resolve complaint");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error resolving complaint: " + e.getMessage());
        }
    }
}