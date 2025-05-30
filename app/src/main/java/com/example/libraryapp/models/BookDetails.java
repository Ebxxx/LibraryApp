package com.example.libraryapp.models;

import java.util.Date;

public class BookDetails {
    private String author;
    private String isbn;
    private String publisher;
    private String edition;
    private Date publicationDate;

    // Getters and Setters
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
} 