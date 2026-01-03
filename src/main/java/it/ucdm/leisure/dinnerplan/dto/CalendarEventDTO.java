package it.ucdm.leisure.dinnerplan.dto;

import java.time.LocalDateTime;

public class CalendarEventDTO {
    private Long id;
    private String title;
    private LocalDateTime date;
    private String type; // "DEADLINE" or "EVENT"
    private String description;

    public CalendarEventDTO(Long id, String title, LocalDateTime date, String type, String description) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.type = type;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
