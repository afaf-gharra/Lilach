package com.lilach.client.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lilach.client.controllers.LoginController;
import com.lilach.client.controllers.OrderHistoryController.Order;
import com.lilach.client.models.ComplaintDTO;
import com.lilach.client.models.OrderDTO;
import com.lilach.client.models.ProductDTO;
import com.lilach.client.models.StoreDTO;
import com.lilach.client.models.UserDTO;
import okhttp3.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApiService {
    private static final String BASE_URL = "http://localhost:8080/api/";
    private static final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build();;
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
    //update order status

    public static OrderDTO updateOrderStatus(int orderId, String status) throws IOException {
        String json = String.format("{\"status\":\"%s\"}", status);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request request = new Request.Builder()
            .url(BASE_URL + "orders/" + orderId + "/status")
            .put(body)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return mapper.readValue(response.body().string(), OrderDTO.class);
            }
            return null;
        }
    }

    public static UserDTO getUserById(int userId) throws IOException {
        Request request = new Request.Builder()
            .url(BASE_URL + "users/" + userId)
            .get()
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return mapper.readValue(response.body().string(), UserDTO.class);
            }
            return null;
        }
    }

    //create product
    public static ProductDTO createProduct(ProductDTO product) throws IOException {
        String json = mapper.writeValueAsString(product);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request request = new Request.Builder()
            .url(BASE_URL + "products")
            .post(body)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return mapper.readValue(response.body().string(), ProductDTO.class);
            }
            return null;
        }
    }

    public static List<Order> getOrdersByUserId(int userId) throws IOException {
        Request request = new Request.Builder()
            .url(BASE_URL + "orders/user/" + userId)
            .get()
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return mapper.readValue(
                    response.body().string(), 
                    mapper.getTypeFactory().constructCollectionType(List.class, Order.class)
                );
            }
            return List.of(); // Return empty list if no orders
        }
    }


    // edit product

    public static ProductDTO editProduct(ProductDTO product) throws IOException {
        String json = mapper.writeValueAsString(product);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request request = new Request.Builder()
            .url(BASE_URL + "products/" + product.getId())
            .put(body)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return mapper.readValue(response.body().string(), ProductDTO.class);
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

    //delete product

    public static String deleteProduct(int productId) throws IOException {
        Request request = new Request.Builder()
            .url(BASE_URL + "products/" + productId)
            .delete()
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
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
    // Cart-related API methods
    public static String addToCart(int userId, CartItem item) throws IOException {
        String json = mapper.writeValueAsString(item);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request request = new Request.Builder()
            .url(BASE_URL + "cart/" + userId + "/add")
            .post(body)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
            return null;
        }
    }

    public static String updateCartItem(int userId, CartItem item) throws IOException {
        String json = mapper.writeValueAsString(item);
        RequestBody body = RequestBody.create(json, JSON);
        
        Request request = new Request.Builder()
            .url(BASE_URL + "cart/" + userId + "/update")
            .put(body)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
            return null;
        }
    }

    public static String removeFromCart(int userId, int itemId) throws IOException {
        Request request = new Request.Builder()
            .url(BASE_URL + "cart/" + userId + "/remove/" + itemId)
            .delete()
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
            return null;
        }
    }

    public static String clearCart(int userId) throws IOException {
        Request request = new Request.Builder()
            .url(BASE_URL + "cart/" + userId + "/clear")
            .delete()
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
            return null;
        }
    }
    public static OrderDTO createOrder(OrderDTO order) throws IOException {
        mapper.registerModule(new JavaTimeModule());
        order.setUserId(LoginController.loggedInUser.getId());
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

   

    // Add store-related API methods
    public static StoreDTO getStoreByManager(int managerId) throws IOException {
        Request request = new Request.Builder()
            .url(BASE_URL + "stores/manager/" + managerId)
            .get()
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return mapper.readValue(response.body().string(), StoreDTO.class);
            }
            return null;
        }
    }

    public static List<ProductDTO> getStoreProducts(int storeId) throws IOException {
        Request request = new Request.Builder()
            .url(BASE_URL + "stores/" + storeId + "/products")
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

    public static boolean updateProductStock(int productId, int newStock) throws IOException {
        Request request = new Request.Builder()
            .url(BASE_URL + "products/" + productId + "/stock")
            .put(RequestBody.create(String.valueOf(newStock), JSON))
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        }
    }



    public static List<OrderDTO> getUserOrders(int userId) throws IOException {
        Request request = new Request.Builder()
            .url(BASE_URL + "orders/user/" + userId)
            .get()
            .build();
            mapper.registerModule(new JavaTimeModule());
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                List<OrderDTO> ret = null;
                try {
                     ret = mapper.readValue(
                        response.body().string(), 
                        mapper.getTypeFactory().constructCollectionType(List.class, OrderDTO.class)
                    );
                } catch (Exception e) {
                    System.err.println("Error parsing orders: " + e.getMessage());
                }
    // TODO: handle exception
                return ret;
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

}