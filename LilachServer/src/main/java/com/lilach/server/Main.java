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
import com.lilach.server.services.WebSocketBroadcaster;

import io.javalin.Javalin;
//fffffff
public class Main {
    public static void main(String[] args) throws JsonMappingException, JsonProcessingException {


        
        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
            // Dev logging disabled to reduce console noise
        }).start(8080);
        
    // Reset any stale online flags from previous runs
    int resetCount = com.lilach.server.services.UserService.resetAllOnlineFlags();
    System.out.println("Reset isOnline flags for " + resetCount + " users");

    app.ws("/ws", ws -> {
        ws.onConnect(ctx -> WebSocketBroadcaster.addConnection(ctx));
        ws.onClose(ctx -> WebSocketBroadcaster.removeConnection(ctx));
        ws.onError(ctx -> WebSocketBroadcaster.removeConnection(ctx));
    });

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