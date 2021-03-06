package com.group11.cmpt276_project.service.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.group11.cmpt276_project.service.dto.ViolationDto;

/**
 * Represent a single Violation including the id, status, details, and type.
 */
@Entity(primaryKeys = {"id", "lang_code"})
public class Violation {

    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "details")
    private String details;

    @ColumnInfo(name = "type")
    private String type;

    @NonNull
    @ColumnInfo(name = "lang_code")
    private String langCode;

    @ColumnInfo(name = "status_code")
    private String statusCode;

    public Violation() {
    }

    public static class ViolationBuilder {
        private String id;
        private String status;
        private String details;
        private String type;
        private String statusCode;
        private String langCode;

        public ViolationBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public ViolationBuilder withStatus(String status) {
            this.status = status;
            return this;
        }

        public ViolationBuilder withDetails(String details) {
            this.details = details;
            return this;
        }

        public ViolationBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public ViolationBuilder withStatusCodee(String statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public ViolationBuilder withLangCode(String langCode) {
            this.langCode = langCode;
            return this;
        }


        public Violation build() {
            Violation violation = new Violation();
            violation.details = this.details;
            violation.id = this.id;
            violation.status = this.status;
            violation.type = this.type;
            violation.langCode = this.langCode;
            violation.statusCode = this.statusCode;

            return violation;
        }
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

    public String getLangCode() {
        return langCode;
    }

    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
}
