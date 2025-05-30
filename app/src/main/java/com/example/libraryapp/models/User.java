package com.example.libraryapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class User {
    @SerializedName("user_id")
    private int userId;
    
    @SerializedName("membership_id")
    private String membershipId;
    
    @SerializedName("username")
    private String username;
    
    @SerializedName("password")
    private String password;
    
    @SerializedName("first_name")
    private String firstName;
    
    @SerializedName("last_name")
    private String lastName;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("role")
    private String role;
    
    @SerializedName("max_books")
    private int maxBooks;
    
    @SerializedName("created_at")
    private Date createdAt;
    
    @SerializedName("updated_at")
    private Date updatedAt;
    
    @SerializedName("borrowing_days_limit")
    private int borrowingDaysLimit;

    // Getters
    public int getUserId() {
        return userId;
    }

    public String getMembershipId() {
        return membershipId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public int getMaxBooks() {
        return maxBooks;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public int getBorrowingDaysLimit() {
        return borrowingDaysLimit;
    }

    // Setters
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setMembershipId(String membershipId) {
        this.membershipId = membershipId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setMaxBooks(int maxBooks) {
        this.maxBooks = maxBooks;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setBorrowingDaysLimit(int borrowingDaysLimit) {
        this.borrowingDaysLimit = borrowingDaysLimit;
    }

    public boolean isValidRole() {
        return role != null && (role.equals("student") || role.equals("faculty"));
    }
} 