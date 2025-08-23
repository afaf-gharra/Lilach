package com.lilach.server.controllers;

import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lilach.server.DTOs.OrderDTO;
import com.lilach.server.models.Order;
import com.lilach.server.models.User;
import com.lilach.server.services.OrderService;
import com.lilach.server.services.UserService;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class OrderController {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static void registerRoutes(io.javalin.Javalin app) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());

        app.post("/api/orders", OrderController::createOrder);
        app.get("/api/orders/user/{userId}", OrderController::getUserOrders);
        app.put("/api/orders/{id}/cancel", OrderController::cancelOrder);
    }
     public static void createOrder(Context ctx) {
        try {

            OrderDTO dto = mapper.readValue(ctx.body(), OrderDTO.class);
            int userId = dto.getUserId();

            User user = UserService.getUserById(userId);

            Order order = mapper.readValue(ctx.body(), Order.class);
            order.setUser(user);
            Order createdOrder = OrderService.createOrder(order);
            ctx.json(createdOrder).status(HttpStatus.CREATED);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error creating order: " + e.getMessage());
        }
    }
    
    public static void getUserOrders(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            List<Order> orders = OrderService.getUserOrders(userId);
            ctx.json(orders).status(HttpStatus.OK);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error retrieving orders: " + e.getMessage());
        }
    }
    
    public static void cancelOrder(Context ctx) {
        try {
            int orderId = Integer.parseInt(ctx.pathParam("id"));
            Order cancelledOrder = OrderService.cancelOrder(orderId);
            if (cancelledOrder != null) {
                ctx.json(cancelledOrder).status(HttpStatus.OK);
            } else {
                ctx.status(HttpStatus.BAD_REQUEST).json("Cannot cancel order");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error cancelling order: " + e.getMessage());
        }
    }
}