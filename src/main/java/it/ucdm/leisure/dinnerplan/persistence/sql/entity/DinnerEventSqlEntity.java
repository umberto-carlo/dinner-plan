package it.ucdm.leisure.dinnerplan.persistence.sql.entity;

import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "dinner_events")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class DinnerEventSqlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private UserSqlEntity organizer;

    @OneToMany(mappedBy = "dinnerEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProposalSqlEntity> proposals = new ArrayList<>();

    private LocalDateTime deadline;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_proposal_date_id")
    private ProposalDateSqlEntity selectedProposalDate;

    @ManyToMany
    @JoinTable(name = "event_participants", joinColumns = @JoinColumn(name = "event_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<UserSqlEntity> participants = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DinnerEventMessageSqlEntity> messages = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private DinnerEvent.EventStatus status;

    public DinnerEventSqlEntity() {
    }

    public DinnerEventSqlEntity(Long id, String title, String description, UserSqlEntity organizer,
            List<ProposalSqlEntity> proposals,
            LocalDateTime deadline, ProposalDateSqlEntity selectedProposalDate, List<UserSqlEntity> participants,
            List<DinnerEventMessageSqlEntity> messages, DinnerEvent.EventStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.organizer = organizer;
        this.proposals = proposals != null ? proposals : new ArrayList<>();
        this.deadline = deadline;
        this.selectedProposalDate = selectedProposalDate;
        this.participants = participants != null ? participants : new ArrayList<>();
        this.messages = messages != null ? messages : new ArrayList<>();
        this.status = status;
    }

    public DinnerEvent toDomain() {
        return new DinnerEvent(
                id,
                title,
                description,
                organizer != null ? organizer.toDomain() : null,
                proposals.stream().map(ProposalSqlEntity::toDomain).collect(Collectors.toList()),
                deadline,
                selectedProposalDate != null ? selectedProposalDate.toDomain() : null,
                participants.stream().map(UserSqlEntity::toDomain).collect(Collectors.toList()),
                messages.stream().map(DinnerEventMessageSqlEntity::toDomain).collect(Collectors.toList()),
                status);
    }

    public static DinnerEventSqlEntity fromDomain(DinnerEvent domain) {
        DinnerEventSqlEntity entity = new DinnerEventSqlEntity();
        entity.setId(domain.getId());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        if (domain.getOrganizer() != null) {
            entity.setOrganizer(UserSqlEntity.fromDomain(domain.getOrganizer()));
        }
        if (domain.getProposals() != null) {
            entity.setProposals(domain.getProposals().stream()
                    .map(ProposalSqlEntity::fromDomain)
                    .collect(Collectors.toList()));
        }
        entity.setDeadline(domain.getDeadline());
        if (domain.getSelectedProposalDate() != null) {
            entity.setSelectedProposalDate(ProposalDateSqlEntity.fromDomain(domain.getSelectedProposalDate()));
        }
        if (domain.getParticipants() != null) {
            entity.setParticipants(domain.getParticipants().stream()
                    .map(UserSqlEntity::fromDomain)
                    .collect(Collectors.toList()));
        }
        entity.setStatus(domain.getStatus());

        // Messages need special handling because they refer back to this entity
        if (domain.getMessages() != null) {
            List<DinnerEventMessageSqlEntity> messageEntities = domain.getMessages().stream()
                    .map(msg -> DinnerEventMessageSqlEntity.fromDomain(msg, entity))
                    .collect(Collectors.toList());
            entity.setMessages(messageEntities);
        }

        return entity;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserSqlEntity getOrganizer() {
        return organizer;
    }

    public void setOrganizer(UserSqlEntity organizer) {
        this.organizer = organizer;
    }

    public List<ProposalSqlEntity> getProposals() {
        return proposals;
    }

    public void setProposals(List<ProposalSqlEntity> proposals) {
        this.proposals = proposals;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public ProposalDateSqlEntity getSelectedProposalDate() {
        return selectedProposalDate;
    }

    public void setSelectedProposalDate(ProposalDateSqlEntity selectedProposalDate) {
        this.selectedProposalDate = selectedProposalDate;
    }

    public List<UserSqlEntity> getParticipants() {
        return participants;
    }

    public void setParticipants(List<UserSqlEntity> participants) {
        this.participants = participants;
    }

    public List<DinnerEventMessageSqlEntity> getMessages() {
        return messages;
    }

    public void setMessages(List<DinnerEventMessageSqlEntity> messages) {
        this.messages = messages;
    }

    public DinnerEvent.EventStatus getStatus() {
        return status;
    }

    public void setStatus(DinnerEvent.EventStatus status) {
        this.status = status;
    }
}
