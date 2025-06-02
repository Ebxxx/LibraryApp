package com.example.libraryapp.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 * Model class representing the library_resources table in the database.
 * This is the main resource entity that can be a book, periodical, or media.
 * Detailed information comes from separate tables (books, periodicals, media_resources).
 */
public class LibraryResource {
    @SerializedName("resource_id")
    private int resourceId;        // Primary key
    
    @SerializedName("title")
    private String title;          // NOT NULL
    
    @SerializedName("accession_number")
    private String accessionNumber; // NOT NULL UNIQUE
    
    @SerializedName("category")
    private String category;       // NOT NULL (enum: book, periodical, media)
    
    @SerializedName("status")
    private String status;         // Default: 'borrowed' (enum: resource_status)
    
    @SerializedName("created_at")
    private Date createdAt;        // Default: CURRENT_TIMESTAMP
    
    @SerializedName("updated_at")
    private Date updatedAt;        // Default: CURRENT_TIMESTAMP
    
    @SerializedName("cover_image")
    private String coverImage;     // Optional cover image URL

    // Related details (populated from separate tables via joins)
    private BookDetails bookDetails;
    private PeriodicalDetails periodicalDetails;
    private MediaDetails mediaDetails;

    // Getters and Setters
    public int getResourceId() { return resourceId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAccessionNumber() { return accessionNumber; }
    public void setAccessionNumber(String accessionNumber) { this.accessionNumber = accessionNumber; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public BookDetails getBookDetails() { return bookDetails; }
    public void setBookDetails(BookDetails bookDetails) { this.bookDetails = bookDetails; }

    public PeriodicalDetails getPeriodicalDetails() { return periodicalDetails; }
    public void setPeriodicalDetails(PeriodicalDetails periodicalDetails) { this.periodicalDetails = periodicalDetails; }

    public MediaDetails getMediaDetails() { return mediaDetails; }
    public void setMediaDetails(MediaDetails mediaDetails) { this.mediaDetails = mediaDetails; }
    
    @Override
    public String toString() {
        return "LibraryResource{" +
                "resourceId=" + resourceId +
                ", title='" + title + '\'' +
                ", accessionNumber='" + accessionNumber + '\'' +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
} 