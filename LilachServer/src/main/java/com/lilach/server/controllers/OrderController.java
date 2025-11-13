package com.lilach.server.controllers;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lilach.server.DTOs.OrderDTO;
import com.lilach.server.models.Order;
import com.lilach.server.models.OrderItem;
import com.lilach.server.models.Product;
import com.lilach.server.models.Store;
import com.lilach.server.models.User;
import com.lilach.server.services.OrderService;
import com.lilach.server.services.ProductService;
import com.lilach.server.services.StoreService;
import com.lilach.server.services.UserService;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class OrderController {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static void registerRoutes(io.javalin.Javalin app) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());

        app.post("/api/orders", OrderController::createOrder);
    app.get("/api/orders", OrderController::getAllOrders);
    app.get("/api/orders/{id}", OrderController::getOrderById);
        app.get("/api/orders/user/{userId}", OrderController::getUserOrders);
        app.put("/api/orders/{id}/cancel", OrderController::cancelOrder);
        // app put status
        app.put("/api/orders/{id}/status", OrderController::updateOrderStatus);
        // get store orders
        app.get("/api/store/{storeId}/orders", OrderController::getStoreOrders);
    }

    public static void getAllOrders(Context ctx) {
        try {
            List<Order> orders = OrderService.getAllOrders();
            ctx.json(orders).status(HttpStatus.OK);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error retrieving all orders: " + e.getMessage());
        }
    }

    public static void getOrderById(Context ctx) {
        try {
            int orderId = Integer.parseInt(ctx.pathParam("id"));
            Order order = OrderService.getOrderById(orderId);
            if (order != null) {
                ctx.json(order).status(HttpStatus.OK);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json("Order not found");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error retrieving order: " + e.getMessage());
        }
    }

    public static void getStoreOrders(Context ctx) {
        try {
            int storeId = Integer.parseInt(ctx.pathParam("storeId"));
            List<Order> orders = OrderService.getStoreOrders(storeId);
            ctx.json(orders).status(HttpStatus.OK);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error retrieving store orders: " + e.getMessage());
        }
    }
    public static void createOrder(Context ctx) {
        try {
            String body = ctx.body();
            System.out.println(body);

            OrderDTO dto = mapper.readValue(ctx.body(), OrderDTO.class);
            Order order = mapper.readValue(ctx.body(), Order.class);
            //convert list orderitemdto to list orderitem dto

            List<OrderItem> items = new ArrayList<>();
            for (var itemDto : order.getItems()) {
                OrderItem orderItem = new OrderItem();
                
                // Check if this is a custom product (null product reference) or regular product
                if (itemDto.getProduct() == null) {
                    // Custom product - no product reference, use custom fields
                    orderItem.setProduct(null);
                    orderItem.setCustomType(itemDto.getCustomType());
                    orderItem.setCustomColor(itemDto.getCustomColor());
                    orderItem.setCustomPriceRange(itemDto.getCustomPriceRange());
                    orderItem.setCustomFlowerTypes(itemDto.getCustomFlowerTypes());
                    orderItem.setCustomSpecialRequests(itemDto.getCustomSpecialRequests());
                    orderItem.setQuantity(itemDto.getQuantity());
                    // No stock check for custom products
                } else {
                    // Regular product - validate and check stock
                    Product product = ProductService.getProductById(itemDto.getProduct().getId());
                    
                    if (product == null) {
                        ctx.status(HttpStatus.BAD_REQUEST).json("Invalid product ID: " + itemDto.getProduct().getId());
                        return;
                    }
                    if (product.getStock() < itemDto.getQuantity()) {
                        ctx.status(HttpStatus.BAD_REQUEST).json("Insufficient stock for product ID: " + itemDto.getProduct().getId());
                        return;
                    }
                    
                    // Reduce stock
                    product.setStock(product.getStock() - itemDto.getQuantity());
                    ProductService.updateProduct(product.getId(), product);
                    
                    orderItem.setProduct(product);
                    orderItem.setQuantity(itemDto.getQuantity());
                    orderItem.setPrice(product.getPrice() * itemDto.getQuantity());
                }
                
                orderItem.setOrder(order); // Set the back-reference to Order
                items.add(orderItem);
            }
            order.setItems(items);
            // Set user
            User user = UserService.getUserById(dto.getUserId());
            if (user == null) {
                ctx.status(HttpStatus.BAD_REQUEST).json("Invalid user ID: " + dto.getUserId());
                return;
            }
            Store s = StoreService.getStoreById(user.getStoreId());
            order.setStore(s);
            order.setUser(user);
   
            // Calculate total price
           // double totalPrice = items.stream().mapToDouble(item -> item.getPrice()).sum();


            //order.setTotalPrice(totalPrice);
            order.setStatus(Order.OrderStatus.PENDING);
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
            boolean cancelledOrder = OrderService.cancelOrder(orderId);
            if (cancelledOrder) {
                ctx.status(HttpStatus.OK).json("Order cancelled successfully");
            } else {
                ctx.status(HttpStatus.BAD_REQUEST).json("Cannot cancel order");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error cancelling order: " + e.getMessage());
        }
    }

    public static void updateOrderStatus(Context ctx) {
        try {
            int orderId = Integer.parseInt(ctx.pathParam("id"));
            // get status form body
            String status = mapper.readTree(ctx.body()).get("status").asText();
            
            Order updatedOrder = OrderService.updateOrderStatus(orderId, status);
            if (updatedOrder != null) {
                ctx.json(updatedOrder).status(HttpStatus.OK);
            } else {
                ctx.status(HttpStatus.BAD_REQUEST).json("Cannot update order status");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error updating order status: " + e.getMessage());
        }
    }
}