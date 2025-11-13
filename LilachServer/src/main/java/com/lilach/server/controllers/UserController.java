package com.lilach.server.controllers;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lilach.server.models.User;
import com.lilach.server.services.UserService;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class UserController {
    private static final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    public static void registerRoutes(io.javalin.Javalin app) {
        app.get("/api/users", UserController::getAllUsers);
        app.get("/api/users/{id}", UserController::getUserById);
        app.post("/api/users", UserController::createUser);
        app.put("/api/users/{id}", UserController::updateUser);
        app.delete("/api/users/{id}", UserController::deleteUser);
    }
    
    public static void getAllUsers(Context ctx) {
        try {
            List<User> users = UserService.getAllUsers();
            // Convert each user to DTO for correct membershipExpiry serialization
            List<Object> userDTOs = users.stream()
                .map(UserController::convertToDTO)
                .toList();
            String response = mapper.writeValueAsString(userDTOs);
            ctx.result(response).contentType("application/json").status(HttpStatus.OK);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error retrieving users: " + e.getMessage());
        }
    }
    
    public static void getUserById(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("id"));
            User user = UserService.getUserById(userId);
            
            if (user != null) {
                String response = mapper.writeValueAsString(convertToDTO(user));
                ctx.result(response).contentType("application/json").status(HttpStatus.OK);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json("User not found");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error retrieving user: " + e.getMessage());
        }
    }
    
    public static void createUser(Context ctx) {
        try {
            User user = mapper.readValue(ctx.body(), User.class);

            // Handle membershipExpiry as string if present
            String rawBody = ctx.body();
            if (rawBody.contains("membershipExpiry")) {
                try {
                    String expiryStr = mapper.readTree(rawBody).get("membershipExpiry").asText();
                    if (expiryStr != null && !expiryStr.isEmpty()) {
                        user.setMembershipExpiry(java.time.LocalDate.parse(expiryStr));
                    }
                } catch (Exception ignored) {}
            }

            User createdUser = UserService.createUser(user);
            ctx.json(createdUser).status(HttpStatus.CREATED);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error creating user: " + e.getMessage());
        }
    }
    
    public static void updateUser(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("id"));
            User userUpdates = mapper.readValue(ctx.body(), User.class);
            
            User updatedUser = UserService.updateUser(userId, userUpdates);
            if (updatedUser != null) {
                // Convert to DTO with proper date formatting
                String response = mapper.writeValueAsString(convertToDTO(updatedUser));
                ctx.result(response).contentType("application/json").status(HttpStatus.OK);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json("User not found");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error updating user: " + e.getMessage());
        }
    }
    
    private static Object convertToDTO(User user) {
        return new Object() {
            public final int id = user.getId();
            public final String username = user.getUsername();
            public final String fullName = user.getFullName();
            public final String email = user.getEmail();
            public final String phone = user.getPhone();
            public final String role = user.getRole().name();
            public final String accountType = user.getAccountType().name();
            public final Integer storeId = user.getStoreId();
            public final String creditCard = user.getCreditCard();
            public final boolean active = user.isActive();
            public final boolean isOnline = user.isOnline();
            public final String membershipExpiry = user.getMembershipExpiry() != null 
                ? user.getMembershipExpiry().toString() 
                : null;
        };
    }
    
    public static void deleteUser(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("id"));
            boolean deleted = UserService.deleteUser(userId);
            
            if (deleted) {
                ctx.status(HttpStatus.NO_CONTENT);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json("User not found");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error deleting user: " + e.getMessage());
        }
    }
}