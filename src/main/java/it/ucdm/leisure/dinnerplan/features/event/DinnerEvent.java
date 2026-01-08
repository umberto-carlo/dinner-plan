package it.ucdm.leisure.dinnerplan.features.event;

import it.ucdm.leisure.dinnerplan.features.proposal.Proposal;
import it.ucdm.leisure.dinnerplan.features.user.User;
import it.ucdm.leisure.dinnerplan.features.proposal.ProposalDate;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dinner_events")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class DinnerEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private User organizer;

    @ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    @JoinTable(name = "event_proposals", joinColumns = @JoinColumn(name = "event_id"), inverseJoinColumns = @JoinColumn(name = "proposal_id"))
    private List<Proposal> proposals = new ArrayList<>();

    private LocalDateTime deadline;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_proposal_date_id")
    private ProposalDate selectedProposalDate;

    @ManyToMany
    @JoinTable(name = "event_participants", joinColumns = @JoinColumn(name = "event_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> participants = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DinnerEventMessage> messages = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    public enum EventStatus {
        OPEN,
        CLOSED,
        DECIDED
    }

    public DinnerEvent() {
    }

    public DinnerEvent(Long id, String title, String description, User organizer, List<Proposal> proposals,
            LocalDateTime deadline, ProposalDate selectedProposalDate, List<User> participants,
            List<DinnerEventMessage> messages, EventStatus status) {
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

    public static DinnerEventBuilder builder() {
        return new DinnerEventBuilder();
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

    public User getOrganizer() {
        return organizer;
    }

    public void setOrganizer(User organizer) {
        this.organizer = organizer;
    }

    public List<Proposal> getProposals() {
        return proposals;
    }

    public void setProposals(List<Proposal> proposals) {
        this.proposals = proposals;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public ProposalDate getSelectedProposalDate() {
        return selectedProposalDate;
    }

    public void setSelectedProposalDate(ProposalDate selectedProposalDate) {
        this.selectedProposalDate = selectedProposalDate;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    public List<DinnerEventMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<DinnerEventMessage> messages) {
        this.messages = messages;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public static class DinnerEventBuilder {
        private Long id;
        private String title;
        private String description;
        private User organizer;
        private List<Proposal> proposals = new ArrayList<>();
        private LocalDateTime deadline;
        private ProposalDate selectedProposalDate;
        private List<User> participants = new ArrayList<>();
        private List<DinnerEventMessage> messages = new ArrayList<>();
        private EventStatus status;

        public DinnerEventBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public DinnerEventBuilder title(String title) {
            this.title = title;
            return this;
        }

        public DinnerEventBuilder description(String description) {
            this.description = description;
            return this;
        }

        public DinnerEventBuilder organizer(User organizer) {
            this.organizer = organizer;
            return this;
        }

        public DinnerEventBuilder proposals(List<Proposal> proposals) {
            this.proposals = proposals;
            return this;
        }

        public DinnerEventBuilder deadline(LocalDateTime deadline) {
            this.deadline = deadline;
            return this;
        }

        public DinnerEventBuilder selectedProposalDate(ProposalDate selectedProposalDate) {
            this.selectedProposalDate = selectedProposalDate;
            return this;
        }

        public DinnerEventBuilder participants(List<User> participants) {
            this.participants = participants;
            return this;
        }

        public DinnerEventBuilder messages(List<DinnerEventMessage> messages) {
            this.messages = messages;
            return this;
        }

        public DinnerEventBuilder status(EventStatus status) {
            this.status = status;
            return this;
        }

        public DinnerEvent build() {
            return new DinnerEvent(id, title, description, organizer, proposals, deadline, selectedProposalDate,
                    participants, messages, status);
        }
    }
}
