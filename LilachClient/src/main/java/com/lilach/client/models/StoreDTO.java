package com.lilach.client.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StoreDTO {
    @Override
    public String toString() {
        return name != null ? name : ("Store #" + id);
    }
    private int id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private boolean active;
    private int storeDiscount = 0; // Store-wide discount percentage [0-100]
    
    // Default constructor
    public StoreDTO() {}
    
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public int getStoreDiscount() { return storeDiscount; }
    public void setStoreDiscount(int storeDiscount) { this.storeDiscount = storeDiscount; }
}