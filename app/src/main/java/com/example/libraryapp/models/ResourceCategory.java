package com.example.libraryapp.models;

/**
 * Enum representing the category field in library_resources table.
 * Matches the database enum values: book, periodical, media
 */
public enum ResourceCategory {
    BOOK("book"),
    PERIODICAL("periodical"), 
    MEDIA("media");

    private final String value;

    ResourceCategory(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    /**
     * Get enum from string value
     */
    public static ResourceCategory fromValue(String value) {
        if (value == null) return null;
        
        for (ResourceCategory category : ResourceCategory.values()) {
            if (category.value.equalsIgnoreCase(value.trim())) {
                return category;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return value;
    }
} 