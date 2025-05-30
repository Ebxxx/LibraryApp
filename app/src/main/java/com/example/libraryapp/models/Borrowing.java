package com.example.libraryapp.models;

import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.JsonAdapter;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Model class representing the borrowings table in the database.
 * Tracks book borrowing transactions and their status.
 */
public class Borrowing {
    @SerializedName("borrowing_id")
    private Integer borrowingId;        // Primary key - nullable for new objects
    
    @SerializedName("user_id")
    private int userId;             // Foreign key to users table
    
    @SerializedName("resource_id")
    private int resourceId;         // Foreign key to library_resources table
    
    @SerializedName("borrow_date")
    private Date borrowDate;        // Default: CURRENT_TIMESTAMP
    
    @SerializedName("due_date")
    private Date dueDate;
    
    @SerializedName("return_date")
    private Date returnDate;
    
    @SerializedName("fine_amount")
    private BigDecimal fineAmount;  // Default: 0.00
    
    @SerializedName("status")
    private String status;          // Enum: borrowing_status (default: 'pending')
    
    @SerializedName("approved_by")
    private Integer approvedBy;     // Foreign key to users table
    
    @SerializedName("approved_at")
    private Date approvedAt;
    
    @SerializedName("returned_by")
    private Integer returnedBy;     // Foreign key to users table

    // Related objects (for joined queries)
    private User user;
    private LibraryResource resource;
    private User approvedByUser;
    private User returnedByUser;

    // Default constructor
    public Borrowing() {}

    // Getters and Setters
    public Integer getBorrowingId() { return borrowingId; }
    public void setBorrowingId(Integer borrowingId) { this.borrowingId = borrowingId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getResourceId() { return resourceId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }

    public Date getBorrowDate() { return borrowDate; }
    public void setBorrowDate(Date borrowDate) { this.borrowDate = borrowDate; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public Date getReturnDate() { return returnDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }

    public BigDecimal getFineAmount() { return fineAmount; }
    public void setFineAmount(BigDecimal fineAmount) { this.fineAmount = fineAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Integer approvedBy) { this.approvedBy = approvedBy; }

    public Date getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Date approvedAt) { this.approvedAt = approvedAt; }

    public Integer getReturnedBy() { return returnedBy; }
    public void setReturnedBy(Integer returnedBy) { this.returnedBy = returnedBy; }

    // Related objects
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LibraryResource getResource() { return resource; }
    public void setResource(LibraryResource resource) { this.resource = resource; }

    public User getApprovedByUser() { return approvedByUser; }
    public void setApprovedByUser(User approvedByUser) { this.approvedByUser = approvedByUser; }

    public User getReturnedByUser() { return returnedByUser; }
    public void setReturnedByUser(User returnedByUser) { this.returnedByUser = returnedByUser; }

    // Helper methods
    public boolean isOverdue() {
        return dueDate != null && returnDate == null && new Date().after(dueDate);
    }

    public boolean isReturned() {
        return returnDate != null;
    }

    public boolean isPending() {
        return "pending".equals(status);
    }

    public boolean isApproved() {
        return "approved".equals(status);
    }

    @Override
    public String toString() {
        return "Borrowing{" +
                "borrowingId=" + borrowingId +
                ", userId=" + userId +
                ", resourceId=" + resourceId +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", status='" + status + '\'' +
                '}';
    }
} 