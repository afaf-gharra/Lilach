package com.lilach.client.controllers;

import java.io.IOException;
import java.util.List;

import org.kordamp.ikonli.javafx.FontIcon;

import com.lilach.client.models.ProductDTO;
import com.lilach.client.services.ApiService;
import com.lilach.client.services.CartItem;
import com.lilach.client.services.CartService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CatalogController extends BaseController  {
    @FXML private Label welcomeLabel;
    @FXML private FlowPane productsContainer;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button customArrangementButton;
    @FXML private Button viewCartButton;
    @FXML private Button viewOrdersButton;
    @FXML private Button loginLogoutButton;
    
    private List<ProductDTO> allProducts;
    
    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome to Lilach Flower Shop!");
        
        if(loggedInUser != null) {
            welcomeLabel.setText("Welcome, " + loggedInUser.getUsername() + "!");
            loginLogoutButton.setText("Logout");
        }
        else {
            loginLogoutButton.setText("Login");
        }

        setupCategoryFilter();
        loadProducts();
        setupButtonIcons();
    }
    
    private void setupButtonIcons() {
        viewCartButton = new Button();
        viewOrdersButton = new Button();
        searchButton.setGraphic(new FontIcon("fas-search"));
        customArrangementButton.setGraphic(new FontIcon("fas-plus-circle"));
        viewCartButton.setGraphic(new FontIcon("fas-shopping-cart"));
        viewOrdersButton.setGraphic(new FontIcon("fas-history"));
    }
    
    private void setupCategoryFilter() {
        categoryComboBox.getItems().addAll(
            "All Products",
            "Bouquet",
            "box",
            "Accessories",
            "Sale"
        );
        categoryComboBox.setValue("All Products");
        categoryComboBox.setOnAction(e -> filterProducts());
    }
    
    private void loadProducts() {
        try {
            if (loggedInUser == null) {
                // Not logged in - show all products
                allProducts = ApiService.getAllProducts();
            }
            else if ("member".equalsIgnoreCase(loggedInUser.getAccountType())) {
                // Member users can see all products
                allProducts = ApiService.getAllProducts();
            }
            else if ("chain".equalsIgnoreCase(loggedInUser.getAccountType())) {
                // Chain users can see all products
                allProducts = ApiService.getAllProducts();
            }
            else {
                // Default case (store employees, etc) - show store-specific products
                allProducts = ApiService.getStoreProducts(getLoggedInUser().getStoreId());
            }
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
            String imagePath = "/com/lilach/client/images/" + product.getCategory() + "/" + product.getImageUrl() + ".jpg";
            System.out.println("Loading image from path: " + imagePath);
            imageView.setImage(new Image(getClass().getResourceAsStream(imagePath)));
            imageView.setFitWidth(180);
            imageView.setFitHeight(180);
            imageView.setPreserveRatio(true);

        } catch (Exception e) {
            System.out.println("Image not found: " + e.getMessage());
            imageView.setImage(new Image(getClass().getResourceAsStream("/com/lilach/client/images/logo.png")));
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
        
        try {
            if (product.getStock() <= 0) {
                addToCartButton.setText("Out of Stock");
                addToCartButton.setDisable(true);
                addToCartButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            } else {
                // Ensure enabled and default text/style for in-stock items
                addToCartButton.setText("Add to Cart");
                addToCartButton.setDisable(false);
                addToCartButton.setStyle(null);
            }
        } catch (Exception ex) {
            // If product.getStock() doesn't exist or fails, leave button as default
            System.out.println("Stock check skipped: " + ex.getMessage());
        }
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
    
    @FXML
    private void handleLoginLogout() {
        if (loggedInUser != null) {
            // User is logged in, so logout
            logout();
        } else {
            // User is not logged in, navigate to login
            navigateToLogin();
        }
    }
    
    @FXML
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
            stage.setScene(new Scene(root, 1600, 900));
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


    @FXML
    private void handleComplaints() {
        navigateTo("/com/lilach/client/views/complaints.fxml", "Complaints Form");
    }

    @FXML
    private void navigateToCatalog() {
        // Already on catalog, do nothing or refresh
        initialize();
    }

    // @Override
    // protected void addNavigationButtons() {
    //     addNavButton("Cart", "fas-shopping-cart", this::handleViewCart);
    //     addNavButton("Orders", "fas-history", this::handleViewOrders);
    //     addNavButton("Custom", "fas-plus", this::handleCreateCustom);
    // }

    

}