package com.example.libraryapp.models;

import java.util.Date;

public class LibraryResource {
    private int resourceId;
    private String title;
    private String accessionNumber;
    private String category;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private String coverImage;
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
} 