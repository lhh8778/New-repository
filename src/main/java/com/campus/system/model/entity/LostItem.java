package com.campus.system.model.entity;
public class LostItem {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String publisherUsername;
    private String claimantUsername;
    private ItemStatus status = ItemStatus.UNCLAIMED;
    public LostItem() {}
    public LostItem(Long id, String t, String d, String loc, String pub) {
        this.id = id; this.title = t; this.description = d; this.location = loc; this.publisherUsername = pub;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }
    public void setClaimantUsername(String u) { this.claimantUsername = u; }
}
