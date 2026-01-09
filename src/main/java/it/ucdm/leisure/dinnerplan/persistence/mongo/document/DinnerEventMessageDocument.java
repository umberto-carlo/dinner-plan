package it.ucdm.leisure.dinnerplan.persistence.mongo.document;

import it.ucdm.leisure.dinnerplan.model.DinnerEventMessage;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "dinner_event_messages")
public class DinnerEventMessageDocument {

    @Id
    private Long id;
    private Long eventId;
    private UserDocument sender;
    private String content;
    private LocalDateTime timestamp;

    public DinnerEventMessageDocument() {
    }

    public DinnerEventMessageDocument(Long id, Long eventId, UserDocument sender, String content,
            LocalDateTime timestamp) {
        this.id = id;
        this.eventId = eventId;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    public DinnerEventMessage toDomain() {
        return new DinnerEventMessage(id, sender != null ? sender.toDomain() : null, content, timestamp, null);
    }

    public static DinnerEventMessageDocument fromDomain(DinnerEventMessage message, Long eventId) {
        return new DinnerEventMessageDocument(
                message.getId(),
                eventId,
                message.getSender() != null ? UserDocument.fromDomain(message.getSender()) : null,
                message.getContent(),
                message.getTimestamp());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public UserDocument getSender() {
        return sender;
    }

    public void setSender(UserDocument sender) {
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
