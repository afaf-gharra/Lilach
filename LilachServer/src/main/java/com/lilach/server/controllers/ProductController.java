package com.lilach.server.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lilach.server.models.Product;
import com.lilach.server.services.ProductService;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.util.List;

public class ProductController {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static void registerRoutes(io.javalin.Javalin app) {
        app.get("/api/products", ProductController::getAllProducts);
        app.get("/api/products/category/{category}", ProductController::getProductsByCategory);
        app.get("/api/products/search", ProductController::searchProducts);
        app.post("/api/products", ProductController::createProduct);
        app.put("/api/products/{id}", ProductController::updateProduct);
        app.delete("/api/products/{id}", ProductController::deleteProduct);

    }
    

    public static void updateProduct(Context ctx) {
        try {
            int productId = Integer.parseInt(ctx.pathParam("id"));
            Product productUpdates = mapper.readValue(ctx.body(), Product.class);
            
            Product updatedProduct = ProductService.updateProduct(productId, productUpdates);
            if (updatedProduct != null) {
                ctx.json(updatedProduct).status(HttpStatus.OK);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json("Product not found");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error updating product: " + e.getMessage());
        }
    }

    public static void deleteProduct(Context ctx) {
        try {
            int productId = Integer.parseInt(ctx.pathParam("id"));
            boolean deleted = ProductService.deleteProduct(productId);
            if (deleted) {
                ctx.status(HttpStatus.NO_CONTENT);
            } else {
                ctx.status(HttpStatus.NOT_FOUND).json("Product not found");
            }
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error deleting product: " + e.getMessage());
        }
    }

    public static void getAllProducts(Context ctx) {
        try {
            List<Product> products = ProductService.getAllProducts();
            ctx.json(products).status(HttpStatus.OK);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Server error: " + e.getMessage());
        }
    }
    
    public static void createProduct(Context ctx) {
        try {
            Product newProduct = mapper.readValue(ctx.body(), Product.class);
            
            // Validate required fields
            if (newProduct.getName() == null || newProduct.getPrice() <= 0) {
                ctx.status(HttpStatus.BAD_REQUEST).json("Name and valid price are required");
                return;
            }
            
            // Set default availability
            newProduct.setAvailable(true);
            
            // Save new product
            Product createdProduct = ProductService.createProduct(newProduct);
            ctx.json(createdProduct).status(HttpStatus.CREATED);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Error creating product: " + e.getMessage());
        }
    }

    public static void getProductsByCategory(Context ctx) {
        try {
            String category = ctx.pathParam("category");
            List<Product> products = ProductService.getProductsByCategory(category);
            ctx.json(products).status(HttpStatus.OK);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Server error: " + e.getMessage());
        }
    }
    
    public static void searchProducts(Context ctx) {
        try {
            String query = ctx.queryParam("q");
            if (query == null || query.trim().isEmpty()) {
                ctx.status(HttpStatus.BAD_REQUEST).json("Search query parameter 'q' is required");
                return;
            }
            
            List<Product> products = ProductService.searchProducts(query);
            ctx.json(products).status(HttpStatus.OK);
        } catch (Exception e) {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json("Server error: " + e.getMessage());
        }
    }
}