package it.ucdm.leisure.dinnerplan.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dinner_event_messages")
public class DinnerEventMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private DinnerEvent event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public DinnerEventMessage() {
    }

    public DinnerEventMessage(DinnerEvent event, User sender, String content, LocalDateTime timestamp) {
        this.event = event;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DinnerEvent getEvent() {
        return event;
    }

    public void setEvent(DinnerEvent event) {
        this.event = event;
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
}
