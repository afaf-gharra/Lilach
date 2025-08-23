package com.lilach.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lilach.server.models.Store;
import com.lilach.server.services.StoreService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;

public class StoreController {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static void registerRoutes(io.javalin.Javalin app) {
        app.get("/api/stores", StoreController::getAllStores);
        app.get("/api/stores/{id}", StoreController::getStoreById);
        app.post("/api/stores", StoreController::createStore);
        app.put("/api/stores/{id}", StoreController::updateStore);
        app.delete("/api/stores/{id}", StoreController::deleteStore);
    }
    
    public static void getAllStores(Context ctx) {
        try {
            List<Store> stores = StoreService.getAllStores();
            ctx.json(stores).status(HttpStatus.OK);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error retrieving stores: " + e.getMessage());
        }
    }
    
    public static void getStoreById(Context ctx) {
        try {
            int storeId = Integer.parseInt(ctx.pathParam("id"));
            Store store = StoreService.getStoreById(storeId);
            
            if (store != null) {
                ctx.json(store).status(HttpStatus.OK);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json("Store not found");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error retrieving store: " + e.getMessage());
        }
    }
    

    
    public static void createStore(Context ctx) {
        try {
            Store store = mapper.readValue(ctx.body(), Store.class);
            Store createdStore = StoreService.createStore(store);
            ctx.json(createdStore).status(HttpStatus.CREATED);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error creating store: " + e.getMessage());
        }
    }
    
    public static void updateStore(Context ctx) {
        try {
            int storeId = Integer.parseInt(ctx.pathParam("id"));
            Store storeUpdates = mapper.readValue(ctx.body(), Store.class);
            
            Store updatedStore = StoreService.updateStore(storeId, storeUpdates);
            if (updatedStore != null) {
                ctx.json(updatedStore).status(HttpStatus.OK);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json("Store not found");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error updating store: " + e.getMessage());
        }
    }
    
    public static void deleteStore(Context ctx) {
        try {
            int storeId = Integer.parseInt(ctx.pathParam("id"));
            boolean deleted = StoreService.deleteStore(storeId);
            
            if (deleted) {
                ctx.status(HttpStatus.NO_CONTENT);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json("Store not found");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error deleting store: " + e.getMessage());
        }
    }
}