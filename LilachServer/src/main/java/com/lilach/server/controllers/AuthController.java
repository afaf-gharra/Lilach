package com.lilach.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lilach.server.models.User;
import com.lilach.server.services.UserService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

public class AuthController {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static void registerRoutes(io.javalin.Javalin app) {
        app.post("/api/login", AuthController::login);
        app.post("/api/register", AuthController::register);
    }
    
    public static void register(Context ctx) {
        try {
            User newUser = mapper.readValue(ctx.body(), User.class);
            
            // Validate required fields
            if (newUser.getUsername() == null || newUser.getPassword() == null) {
                ctx.status(HttpStatus.BAD_REQUEST).json("Username and password are required");
                return;
            }
            
            //Check if username exists
            if (UserService.usernameExists(newUser.getUsername())) {
                ctx.status(HttpStatus.CONFLICT).json("Username already exists");
                return;
            }
            
            // Set default values
            newUser.setActive(true);
            newUser.setAccountType(User.AccountType.CHAIN); // Default account type
            
            // Save new user
            User createdUser = UserService.createUser(newUser);
            ctx.json(createdUser).status(HttpStatus.CREATED);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error creating user: " + e.getMessage());
        }
    }

    public static void login(Context ctx) {
        try {
            User credentials = mapper.readValue(ctx.body(), User.class);
            User user = UserService.authenticate(
                credentials.getUsername(), 
                credentials.getPassword()
            );
            
            if (user != null) {
                ctx.json(user).status(HttpStatus.OK);
            } else {
                ctx.status(HttpStatus.UNAUTHORIZED).json("Invalid credentials");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Server error: " + e.getMessage());
        }
    }
}