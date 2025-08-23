package com.lilach.client.controllers;

import com.lilach.client.models.ProductDTO;
import com.lilach.client.services.ApiService;
import com.lilach.client.services.CartItem;
import com.lilach.client.services.CartService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.List;

public class CatalogController extends BaseController  {
    @FXML private Label welcomeLabel;
    @FXML private FlowPane productsContainer;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button customArrangementButton;
    @FXML private Button viewCartButton;
    @FXML private Button viewOrdersButton;
    
    private List<ProductDTO> allProducts;
    
    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome to Lilach Flower Shop!");
        setupCategoryFilter();
        loadProducts();
        setupButtonIcons();
    }
    
    private void setupButtonIcons() {
        searchButton.setGraphic(new FontIcon("fas-search"));
        customArrangementButton.setGraphic(new FontIcon("fas-plus-circle"));
        viewCartButton.setGraphic(new FontIcon("fas-shopping-cart"));
        viewOrdersButton.setGraphic(new FontIcon("fas-history"));
    }
    
    private void setupCategoryFilter() {
        categoryComboBox.getItems().addAll(
            "All Products",
            "Flowers",
            "Arrangements",
            "Accessories",
            "Special Offers"
        );
        categoryComboBox.setValue("All Products");
        categoryComboBox.setOnAction(e -> filterProducts());
    }
    
    private void loadProducts() {
        try {
            allProducts = ApiService.getProducts();
            displayProducts(allProducts);
        } catch (IOException e) {
            showError("Connection Error", "Failed to load products: " + e.getMessage());
        }
    }
    
    private void displayProducts(List<ProductDTO> products) {
        productsContainer.getChildren().clear();
        
        for (ProductDTO product : products) {
            VBox productCard = createProductCard(product);
            productsContainer.getChildren().add(productCard);
        }
    }
    
    private VBox createProductCard(ProductDTO product) {
        VBox card = new VBox();
        card.getStyleClass().add("product-card");
        card.setSpacing(10);
        card.setPrefWidth(200);
        
        // Product image
        ImageView imageView = new ImageView();
        try {
            //Image image = new Image(product.getImageUrl(), true);
           // imageView.setImage(image);
            imageView.setFitWidth(180);
            imageView.setFitHeight(180);
            imageView.setPreserveRatio(true);
            imageView.setImage(new Image(getClass().getResourceAsStream("/com/lilach/client/images/logo.png")));

        } catch (Exception e) {
            //imageView.setImage(new Image(getClass().getResourceAsStream("/com/lilach/client/images/logo.png")));
        }
        
        // Product details
        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-name");
        
        Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
        priceLabel.getStyleClass().add("product-price");
        
        Label categoryLabel = new Label(product.getCategory());
        categoryLabel.getStyleClass().add("product-category");
        
        Button addToCartButton = new Button("Add to Cart");
        addToCartButton.getStyleClass().addAll("btn", "btn-primary");
        addToCartButton.setOnAction(e -> addToCart(product));
        
        card.getChildren().addAll(
            imageView,
            nameLabel,
            priceLabel,
            categoryLabel,
            addToCartButton
        );
        
        return card;
    }
    
    @FXML
    private void handleSearch() {
        filterProducts();
    }

    @FXML
    private void handleLogout() {
        logout();
    }
    
    private void filterProducts() {
        String searchTerm = searchField.getText().toLowerCase();
        String selectedCategory = categoryComboBox.getValue();
        
        List<ProductDTO> filtered = allProducts.stream()
            .filter(product -> 
                (selectedCategory.equals("All Products") || 
                 product.getCategory().equalsIgnoreCase(selectedCategory)))
            .filter(product ->
                product.getName().toLowerCase().contains(searchTerm) ||
                product.getDescription().toLowerCase().contains(searchTerm))
            .toList();
        
        displayProducts(filtered);
    }
    
    @FXML
    private void addToCart(ProductDTO product) {
        
        CartItem item = new CartItem(
            product.getId(),
            product.getName(),
            product.getPrice(),
            1,
            product.getImageUrl()
        );
        
        CartService.getInstance().addItem(item);



        showSuccess("Added to Cart", product.getName() + " added to your cart!");
    }
    
    @FXML
    private void handleCreateCustomArrangement() {
        try {
            Stage stage = (Stage) productsContainer.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/lilach/client/views/custom_arrangement.fxml"));
            stage.setScene(new Scene(root, 800, 600));
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Navigation Error", "Failed to load custom arrangement view: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleViewCart() {
        navigateTo("/com/lilach/client/views/cart.fxml", "Shopping Cart");
    }
    
    @FXML
    private void handleViewOrders() {
        navigateTo("/com/lilach/client/views/order_history.fxml", "My Orders");
    }

   
    

    @FXML
    private void handleCreateCustom() {
        navigateTo("/com/lilach/client/views/custom_arrangement.fxml", "Create Custom Arrangement");
    }

    // @Override
    // protected void addNavigationButtons() {
    //     addNavButton("Cart", "fas-shopping-cart", this::handleViewCart);
    //     addNavButton("Orders", "fas-history", this::handleViewOrders);
    //     addNavButton("Custom", "fas-plus", this::handleCreateCustom);
    // }

    

}