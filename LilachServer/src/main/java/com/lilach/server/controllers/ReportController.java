package com.lilach.server.controllers;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lilach.server.services.ReportService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;

public class ReportController {
    private static final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
    
    public static void registerRoutes(io.javalin.Javalin app) {
        app.get("/api/stores/{storeId}/reports/yearly/{year}", ReportController::getYearlyReport);
        app.get("/api/stores/{storeId}/reports/quarterly/{year}/{quarter}", ReportController::getQuarterlyReport);
        app.get("/api/stores/{storeId}/reports/years", ReportController::getAvailableYears);
    }
    
    public static void getYearlyReport(Context ctx) {
        try {
            int storeId = Integer.parseInt(ctx.pathParam("storeId"));
            int year = Integer.parseInt(ctx.pathParam("year"));
            
            var report = ReportService.generateYearlyReport(storeId, year);
            ctx.json(report).status(HttpStatus.OK);
            
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json("Invalid store ID or year format");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error generating yearly report: " + e.getMessage());
        }
    }
    
    public static void getQuarterlyReport(Context ctx) {
        try {
            int storeId = Integer.parseInt(ctx.pathParam("storeId"));
            int year = Integer.parseInt(ctx.pathParam("year"));
            int quarter = Integer.parseInt(ctx.pathParam("quarter"));
            mapper.registerModule(new JavaTimeModule());
            
            if (quarter < 1 || quarter > 4) {
                ctx.status(HttpStatus.BAD_REQUEST).json("Quarter must be between 1 and 4");
                return;
            }
            
            var report = ReportService.generateQuarterlyReport(storeId, year, quarter);
            ctx.json(report).status(HttpStatus.OK);
            
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json("Invalid parameters format");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error generating quarterly report: " + e.getMessage());
        }
    }
    
    public static void getAvailableYears(Context ctx) {
        try {
            int storeId = Integer.parseInt(ctx.pathParam("storeId"));
            List<Integer> years = ReportService.getAvailableYears(storeId);
            ctx.json(years).status(HttpStatus.OK);
            
        } catch (NumberFormatException e) {
            ctx.status(HttpStatus.BAD_REQUEST).json("Invalid store ID format");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error retrieving available years: " + e.getMessage());
        }
    }
}