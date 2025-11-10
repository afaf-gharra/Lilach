package com.lilach.server.controllers;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lilach.server.models.User;
import com.lilach.server.services.UserService;

import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class UserController {
    private static final ObjectMapper mapper = new ObjectMapper();
    
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
            ctx.json(users).status(HttpStatus.OK);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error retrieving users: " + e.getMessage());
        }
    }
    
    public static void getUserById(Context ctx) {
        try {
            int userId = Integer.parseInt(ctx.pathParam("id"));
            User user = UserService.getUserById(userId);
            
            if (user != null) {
                ctx.json(user).status(HttpStatus.OK);
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
                ctx.json(updatedUser).status(HttpStatus.OK);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json("User not found");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error updating user: " + e.getMessage());
        }
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