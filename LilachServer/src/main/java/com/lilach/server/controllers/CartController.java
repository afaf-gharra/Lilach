package com.lilach.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lilach.server.models.CartItemDTO;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class CartController {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<Integer, CartItemDTO> userCarts = new HashMap<>(); // In-memory storage
    
    public static void registerRoutes(io.javalin.Javalin app) {
        app.get("/api/cart/{userId}", CartController::getCart);
        app.post("/api/cart/{userId}/add", CartController::addToCart);
        app.put("/api/cart/{userId}/update", CartController::updateCartItem);
        app.delete("/api/cart/{userId}/remove/{itemId}", CartController::removeFromCart);
        app.delete("/api/cart/{userId}/clear", CartController::clearCart);
    }
    
    public static void getCart(Context ctx) {
        try {
            //int userId = Integer.parseInt(ctx.pathParam("userId"));
            // In a real app, you'd get this from a database
            




            ctx.json(userCarts.values()).status(HttpStatus.OK);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error retrieving cart: " + e.getMessage());
        }
    }
    
    public static void addToCart(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            CartItemDTO item = mapper.readValue(ctx.body(), CartItemDTO.class);
            
            item.setUserId(userId);
            // In a real app, you'd save this to a database
            userCarts.put(item.getProductId(), item);
            
            ctx.status(HttpStatus.OK).json("Item added to cart");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error adding to cart: " + e.getMessage());
        }
    }
    
    public static void updateCartItem(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("userId"));
            CartItemDTO item = mapper.readValue(ctx.body(), CartItemDTO.class);
            
            item.setUserId(userId);
            if (userCarts.containsKey(item.getProductId())) {
                userCarts.put(item.getProductId(), item);
                ctx.status(HttpStatus.OK).json("Cart item updated");
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json("Item not found in cart");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error updating cart: " + e.getMessage());
        }
    }
    
    public static void removeFromCart(Context ctx) {
        try {
            //int userId = Integer.parseInt(ctx.pathParam("userId"));
            int itemId = Integer.parseInt(ctx.pathParam("itemId"));
            
            if (userCarts.remove(itemId) != null) {
                ctx.status(HttpStatus.OK).json("Item removed from cart");
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json("Item not found in cart");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error removing from cart: " + e.getMessage());
        }
    }
    
    public static void clearCart(Context ctx) {
        try {
            //int userId = Integer.parseInt(ctx.pathParam("userId"));
            userCarts.clear();
            ctx.status(HttpStatus.OK).json("Cart cleared");
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error clearing cart: " + e.getMessage());
        }
    }
}