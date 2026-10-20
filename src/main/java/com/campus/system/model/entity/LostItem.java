package com.campus.system.model.entity;

public class LostItem {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String publisherUsername;
    private String claimantUsername;
    private ItemStatus status;

    public LostItem() {}
    public LostItem(Long id, String title, String description, String location, String publisherUsername) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.location = location;
        this.publisherUsername = publisherUsername;
        this.status = ItemStatus.UNCLAIMED;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getPublisherUsername() { return publisherUsername; }
    public String getClaimantUsername() { return claimantUsername; }
    public void setClaimantUsername(String claimantUsername) { this.claimantUsername = claimantUsername; }
    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "LostItem{id=" + id + ", title='" + title + "', location='" + location + 
               "', status=" + status + (claimantUsername != null ? ", claimant='" + claimantUsername + "'" : "") + "}";
    }
}