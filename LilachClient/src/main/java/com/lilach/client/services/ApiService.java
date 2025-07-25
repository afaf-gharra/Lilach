package com.lilach.client.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lilach.client.models.ComplaintDTO;
import com.lilach.client.models.OrderDTO;
import com.lilach.client.models.ProductDTO;
import com.lilach.client.models.UserDTO;
import okhttp3.*;
import java.io.IOException;
import java.util.List;

public class ApiService {
    private static final String BASE_URL = "http://localhost:8080/api/";
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    public static UserDTO login(String username, String password) throws IOException {
        String json = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request request = new Request.Builder()
            .url(BASE_URL + "login")
            .post(body)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return mapper.readValue(response.body().string(), UserDTO.class);
            }
            return null;
        }
    }
    public static void addToCart(ProductDTO product) {

        System.out.println("Added to cart: " + product.getName());
    }

    public static UserDTO register(UserDTO user) throws IOException {
        String json = mapper.writeValueAsString(user);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request request = new Request.Builder()
            .url(BASE_URL + "register")
            .post(body)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return mapper.readValue(response.body().string(), UserDTO.class);
            }
            return null;
        }
    }


    public static OrderDTO createOrder(OrderDTO order) throws IOException {
        String json = mapper.writeValueAsString(order);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request request = new Request.Builder()
            .url(BASE_URL + "orders")
            .post(body)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return mapper.readValue(response.body().string(), OrderDTO.class);
            }
            return null;
        }
    }
    public static List<ProductDTO> getProducts() throws IOException {
        Request request = new Request.Builder()
            .url(BASE_URL + "products")
            .get()
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return mapper.readValue(
                    response.body().string(), 
                    mapper.getTypeFactory().constructCollectionType(List.class, ProductDTO.class)
                );
            }
            return List.of(); // Return empty list if no products
        }
    }

    public static List<OrderDTO> getUserOrders(int userId) throws IOException {
        Request request = new Request.Builder()
            .url(BASE_URL + "orders/user/" + userId)
            .get()
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return mapper.readValue(
                    response.body().string(), 
                    mapper.getTypeFactory().constructCollectionType(List.class, OrderDTO.class)
                );
            }
            return null;
        }
    }

    public static OrderDTO cancelOrder(int orderId) throws IOException {
        Request request = new Request.Builder()
            .url(BASE_URL + "orders/" + orderId + "/cancel")
            .put(RequestBody.create("", JSON))
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return mapper.readValue(response.body().string(), OrderDTO.class);
            }
            return null;
        }
    }

    public static ComplaintDTO createComplaint(ComplaintDTO complaint) throws IOException {
        String json = mapper.writeValueAsString(complaint);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request request = new Request.Builder()
            .url(BASE_URL + "complaints")
            .post(body)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return mapper.readValue(response.body().string(), ComplaintDTO.class);
            }
            return null;
        }
    }

    public static List<ProductDTO> getAllProducts() throws IOException {
        Request request = new Request.Builder()
            .url(BASE_URL + "products")
            .get()
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return mapper.readValue(
                    response.body().string(), 
                    mapper.getTypeFactory().constructCollectionType(List.class, ProductDTO.class)
                );
            }
            return List.of(); // Return empty list if no products
        }
    }

    public static List<ProductDTO> getProductsByCategory(String category) throws IOException {
        Request request = new Request.Builder()
            .url(BASE_URL + "products/category/" + category)
            .get()
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return mapper.readValue(
                    response.body().string(), 
                    mapper.getTypeFactory().constructCollectionType(List.class, ProductDTO.class)
                );
            }
            return List.of();
        }
    }

    public static List<ProductDTO> searchProducts(String query) throws IOException {
        HttpUrl url = HttpUrl.parse(BASE_URL + "products/search").newBuilder()
            .addQueryParameter("q", query)
            .build();
        
        Request request = new Request.Builder()
            .url(url)
            .get()
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return mapper.readValue(
                    response.body().string(), 
                    mapper.getTypeFactory().constructCollectionType(List.class, ProductDTO.class)
                );
            }
            return List.of();
        }
    }

}