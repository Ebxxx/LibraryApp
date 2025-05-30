package com.example.libraryapp.models;

public class MediaDetails {
    private int mediaId;       // Primary key from media_resources table
    private int resourceId;    // Foreign key to library_resources
    private String format;
    private Integer runtime;
    private String mediaType;
    private String type;       // Additional type field from schema

    // Getters and Setters
    public int getMediaId() { return mediaId; }
    public void setMediaId(int mediaId) { this.mediaId = mediaId; }

    public int getResourceId() { return resourceId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public Integer getRuntime() { return runtime; }
    public void setRuntime(Integer runtime) { this.runtime = runtime; }

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
} 