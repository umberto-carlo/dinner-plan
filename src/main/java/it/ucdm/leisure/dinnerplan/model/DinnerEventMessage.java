package it.ucdm.leisure.dinnerplan.model;

import java.time.LocalDateTime;

public class DinnerEventMessage {

    private Long id;
    private User sender;
    private String content;
    private LocalDateTime timestamp;
    private DinnerEvent event;

    public DinnerEventMessage() {
    }

    public DinnerEventMessage(Long id, User sender, String content, LocalDateTime timestamp, DinnerEvent event) {
        this.id = id;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
        this.event = event;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public DinnerEvent getEvent() {
        return event;
    }

    public void setEvent(DinnerEvent event) {
        this.event = event;
    }
}
