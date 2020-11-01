package com.group11.cmpt276_project.service.model;

/**
 * Represent a single Violation including the id, status, details, and type.
 */

public class Violation {
    private String id;
    private String status;
    private String details;
    private String type;

    public Violation() {};

    public Violation(String id, String status, String details, String type) {
        this.id = id;
        this.status = status;
        this.details = details;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
