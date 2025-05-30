package com.example.libraryapp.models;

import java.util.Date;

public class BorrowingHistory {
    private int borrowingId;
    private int userId;
    private int resourceId;
    private String resourceTitle;
    private String resourceCategory;
    private Date borrowDate;
    private Date dueDate;
    private Date returnDate;
    private String status; // ACTIVE, RETURNED, OVERDUE

    // Constructors
    public BorrowingHistory() {}

    public BorrowingHistory(int borrowingId, int userId, int resourceId, String resourceTitle, 
                           String resourceCategory, Date borrowDate, Date dueDate, 
                           Date returnDate, String status) {
        this.borrowingId = borrowingId;
        this.userId = userId;
        this.resourceId = resourceId;
        this.resourceTitle = resourceTitle;
        this.resourceCategory = resourceCategory;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    // Getters and Setters
    public int getBorrowingId() { return borrowingId; }
    public void setBorrowingId(int borrowingId) { this.borrowingId = borrowingId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getResourceId() { return resourceId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }

    public String getResourceTitle() { return resourceTitle; }
    public void setResourceTitle(String resourceTitle) { this.resourceTitle = resourceTitle; }

    public String getResourceCategory() { return resourceCategory; }
    public void setResourceCategory(String resourceCategory) { this.resourceCategory = resourceCategory; }

    public Date getBorrowDate() { return borrowDate; }
    public void setBorrowDate(Date borrowDate) { this.borrowDate = borrowDate; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public Date getReturnDate() { return returnDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
} 