package com.example.libraryapp.models;

import java.util.Date;

public class BookDetails {
    private int bookId;        // Primary key from books table
    private int resourceId;    // Foreign key to library_resources
    private String author;
    private String isbn;
    private String publisher;
    private String edition;
    private Date publicationDate;
    private String type;       // Additional type field from schema

    // Getters and Setters
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public int getResourceId() { return resourceId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }

    public String getEdition() { return edition; }
    public void setEdition(String edition) { this.edition = edition; }

    public Date getPublicationDate() { return publicationDate; }
    public void setPublicationDate(Date publicationDate) { this.publicationDate = publicationDate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
} 