package it.ucdm.leisure.dinnerplan.persistence.sql.entity;

import it.ucdm.leisure.dinnerplan.model.DinnerEventMessage;
import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dinner_event_messages")
public class DinnerEventMessageSqlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private DinnerEventSqlEntity event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserSqlEntity sender;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public DinnerEventMessageSqlEntity() {
    }

    public DinnerEventMessageSqlEntity(DinnerEventSqlEntity event, UserSqlEntity sender, String content,
            LocalDateTime timestamp) {
        this.event = event;
        this.sender = sender;
        this.content = content;
        this.timestamp = timestamp;
    }

    public DinnerEventMessage toDomain() {
        DinnerEvent partialEvent = null;
        if (event != null) {
            partialEvent = new DinnerEvent();
            partialEvent.setId(event.getId());
        }
        return new DinnerEventMessage(id, sender != null ? sender.toDomain() : null, content, timestamp, partialEvent);
    }

    public static DinnerEventMessageSqlEntity fromDomain(DinnerEventMessage domain, DinnerEventSqlEntity eventEntity) {
        DinnerEventMessageSqlEntity entity = new DinnerEventMessageSqlEntity();
        entity.setId(domain.getId());
        if (domain.getSender() != null) {
            entity.setSender(UserSqlEntity.fromDomain(domain.getSender()));
        }
        entity.setContent(domain.getContent());
        entity.setTimestamp(domain.getTimestamp());
        entity.setEvent(eventEntity);
        return entity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DinnerEventSqlEntity getEvent() {
        return event;
    }

    public void setEvent(DinnerEventSqlEntity event) {
        this.event = event;
    }

    public UserSqlEntity getSender() {
        return sender;
    }

    public void setSender(UserSqlEntity sender) {
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
