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
    @SerializedName("users")
    private User user;
    
    @SerializedName("library_resources")
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
        return "approved".equals(status) || "active".equals(status);
    }
    
    // Get formatted resource details for display
    public String getResourceDisplayTitle() {
        if (resource != null && resource.getTitle() != null) {
            return resource.getTitle();
        }
        return "Unknown Resource";
    }
    
    public String getResourceDisplayCategory() {
        if (resource != null && resource.getCategory() != null) {
            switch (resource.getCategory().toLowerCase()) {
                case "book": return "📚 Book";
                case "periodical": return "📰 Periodical";
                case "media": return "🎬 Media";
                default: return "📄 " + resource.getCategory();
            }
        }
        return "📄 Unknown";
    }
    
    public String getResourceDisplayDetails() {
        if (resource == null) return "Resource details not available";
        
        StringBuilder details = new StringBuilder();
        String category = resource.getCategory();
        
        if ("book".equals(category) && resource.getBookDetails() != null) {
            BookDetails book = resource.getBookDetails();
            if (book.getAuthor() != null && !book.getAuthor().isEmpty()) {
                details.append("by ").append(book.getAuthor());
            }
            if (book.getPublisher() != null && !book.getPublisher().isEmpty()) {
                if (details.length() > 0) details.append(" • ");
                details.append(book.getPublisher());
            }
            if (book.getEdition() != null && !book.getEdition().isEmpty()) {
                if (details.length() > 0) details.append(" • ");
                details.append(book.getEdition());
            }
            if (book.getIsbn() != null && !book.getIsbn().isEmpty()) {
                if (details.length() > 0) details.append(" • ");
                details.append("ISBN: ").append(book.getIsbn());
            }
        } else if ("periodical".equals(category) && resource.getPeriodicalDetails() != null) {
            PeriodicalDetails periodical = resource.getPeriodicalDetails();
            if (periodical.getVolume() != null && !periodical.getVolume().isEmpty()) {
                details.append("Vol. ").append(periodical.getVolume());
            }
            if (periodical.getIssue() != null && !periodical.getIssue().isEmpty()) {
                if (details.length() > 0) details.append(" • ");
                details.append("Issue ").append(periodical.getIssue());
            }
            if (periodical.getIssn() != null && !periodical.getIssn().isEmpty()) {
                if (details.length() > 0) details.append(" • ");
                details.append("ISSN: ").append(periodical.getIssn());
            }
        } else if ("media".equals(category) && resource.getMediaDetails() != null) {
            MediaDetails media = resource.getMediaDetails();
            if (media.getFormat() != null && !media.getFormat().isEmpty()) {
                details.append(media.getFormat());
            }
            if (media.getMediaType() != null && !media.getMediaType().isEmpty()) {
                if (details.length() > 0) details.append(" • ");
                details.append(media.getMediaType());
            }
            if (media.getRuntime() != null && media.getRuntime() > 0) {
                if (details.length() > 0) details.append(" • ");
                details.append(media.getRuntime()).append(" min");
            }
        }
        
        // Add accession number if available
        if (resource.getAccessionNumber() != null) {
            if (details.length() > 0) details.append(" • ");
            details.append("Acc: ").append(resource.getAccessionNumber());
        }
        
        return details.length() > 0 ? details.toString() : "Details loading from database...";
    }
    
    // Get formatted status with emoji
    public String getFormattedStatus() {
        if (status == null) return "❓ Unknown";
        
        switch (status.toLowerCase()) {
            case "pending": return "⏳ Pending";
            case "approved": return "✅ Approved";
            case "active": return "📖 Active";
            case "returned": return "✅ Returned";
            case "overdue": return "⚠️ Overdue";
            case "rejected": return "❌ Rejected";
            default: return "❓ " + status;
        }
    }
    
    // Get formatted dates
    public String getFormattedBorrowDate() {
        if (borrowDate == null) return "Not set";
        return new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            .format(borrowDate);
    }
    
    public String getFormattedDueDate() {
        if (dueDate == null) return "Not set";
        return new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            .format(dueDate);
    }
    
    public String getFormattedReturnDate() {
        if (returnDate == null) return "Not returned";
        return new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            .format(returnDate);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Borrowing{");
        sb.append("borrowingId=").append(borrowingId);
        sb.append(", userId=").append(userId);
        sb.append(", resourceId=").append(resourceId);
        sb.append(", status='").append(status).append('\'');
        
        // Add resource information if available
        if (resource != null) {
            sb.append(", resourceTitle='").append(resource.getTitle()).append('\'');
            sb.append(", resourceCategory='").append(resource.getCategory()).append('\'');
            if (resource.getAccessionNumber() != null) {
                sb.append(", accession='").append(resource.getAccessionNumber()).append('\'');
            }
        }
        
        // Add important dates
        if (borrowDate != null) {
            sb.append(", borrowDate=").append(getFormattedBorrowDate());
        }
        if (dueDate != null) {
            sb.append(", dueDate=").append(getFormattedDueDate());
        }
        if (returnDate != null) {
            sb.append(", returnDate=").append(getFormattedReturnDate());
        }
        
        // Add fine information if applicable
        if (fineAmount != null && fineAmount.compareTo(java.math.BigDecimal.ZERO) > 0) {
            sb.append(", fineAmount=").append(fineAmount);
        }
        
        sb.append('}');
        return sb.toString();
    }
} 