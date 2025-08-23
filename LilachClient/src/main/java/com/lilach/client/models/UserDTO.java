package com.lilach.client.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {
    private int id;
    private String username;
    private String fullName;
    private String accountType;
    @JsonProperty("isActive")
    private boolean isActive;
    private String password; 
    private String email;
    private String phone;
    private String creditCard;
    private String role; // Assuming role is a string, adjust as necessary

    private Integer storeId;
    private StoreDTO store;

    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }

    public StoreDTO getStore() { return store; }
    public void setStore(StoreDTO store) { this.store = store; }
   

    // Default constructor for JSON deserialization
    public UserDTO() {}

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCreditCard() { return creditCard; }
    public void setCreditCard(String creditCard) { this.creditCard = creditCard; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}