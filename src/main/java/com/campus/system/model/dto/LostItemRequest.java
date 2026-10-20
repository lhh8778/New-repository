package com.campus.system.model.dto;

public class LostItemRequest {
    private String title;
    private String description;
    private String location;
    private String publisherUsername;

    public LostItemRequest() {}
    public LostItemRequest(String title, String description, String location, String publisherUsername) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.publisherUsername = publisherUsername;
    }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getPublisherUsername() { return publisherUsername; }
}