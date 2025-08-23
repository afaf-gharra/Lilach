package com.lilach.server;

import io.javalin.Javalin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.lilach.server.controllers.*;
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
        
        System.out.println("Server running on http://localhost:8080");
    }
}