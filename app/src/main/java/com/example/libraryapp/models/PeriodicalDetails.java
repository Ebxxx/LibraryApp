package com.example.libraryapp.models;

import java.util.Date;

public class PeriodicalDetails {
    private String issn;
    private String volume;
    private String issue;
    private Date publicationDate;

    // Getters and Setters
    public String getIssn() { return issn; }
    public void setIssn(String issn) { this.issn = issn; }

    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }

    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }

    public Date getPublicationDate() { return publicationDate; }
    public void setPublicationDate(Date publicationDate) { this.publicationDate = publicationDate; }
} 