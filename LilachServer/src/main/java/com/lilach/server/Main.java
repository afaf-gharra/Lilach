package com.lilach.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.lilach.server.controllers.AuthController;
import com.lilach.server.controllers.CartController;
import com.lilach.server.controllers.ComplaintController;
import com.lilach.server.controllers.OrderController;
import com.lilach.server.controllers.ProductController;
import com.lilach.server.controllers.RefundController;
import com.lilach.server.controllers.ReportController;
import com.lilach.server.controllers.StoreController;
import com.lilach.server.controllers.UserController;

import io.javalin.Javalin;
//fffffff
public class Main {
    public static void main(String[] args) throws JsonMappingException, JsonProcessingException {


        
        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
            config.plugins.enableDevLogging();
        }).start(8080);
        
        // Register controllers
        AuthController.registerRoutes(app);
        ProductController.registerRoutes(app);
        OrderController.registerRoutes(app);
        CartController.registerRoutes(app);
        StoreController.registerRoutes(app);
        UserController.registerRoutes(app);
        ComplaintController.registerRoutes(app);
        ReportController.registerRoutes(app);
        RefundController.registerRoutes(app);
        
        System.out.println("Server running on http://localhost:8080");
    }
}