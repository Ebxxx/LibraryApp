package com.example.libraryapp.models;

import java.util.Date;

public class PeriodicalDetails {
    private int periodicalId;  // Primary key from periodicals table
    private int resourceId;    // Foreign key to library_resources
    private String issn;
    private String volume;
    private String issue;
    private Date publicationDate;
    private String type;       // Additional type field from schema

    // Getters and Setters
    public int getPeriodicalId() { return periodicalId; }
    public void setPeriodicalId(int periodicalId) { this.periodicalId = periodicalId; }

    public int getResourceId() { return resourceId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }

    public String getIssn() { return issn; }
    public void setIssn(String issn) { this.issn = issn; }

    public String getVolume() { return volume; }
    public void setVolume(String volume) { this.volume = volume; }

    public String getIssue() { return issue; }
    public void setIssue(String issue) { this.issue = issue; }

    public Date getPublicationDate() { return publicationDate; }
    public void setPublicationDate(Date publicationDate) { this.publicationDate = publicationDate; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
} 