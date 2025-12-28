package it.ucdm.leisure.dinnerplan.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dinner_events")
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

    @OneToMany(mappedBy = "dinnerEvent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Proposal> proposals = new ArrayList<>();

    private LocalDateTime deadline;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_proposal_id")
    private Proposal selectedProposal;

    @ManyToMany
    @JoinTable(name = "event_participants", joinColumns = @JoinColumn(name = "event_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> participants = new ArrayList<>();

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
            LocalDateTime deadline, Proposal selectedProposal, List<User> participants, EventStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.organizer = organizer;
        this.proposals = proposals != null ? proposals : new ArrayList<>();
        this.deadline = deadline;
        this.selectedProposal = selectedProposal;
        this.participants = participants != null ? participants : new ArrayList<>();
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

    public Proposal getSelectedProposal() {
        return selectedProposal;
    }

    public void setSelectedProposal(Proposal selectedProposal) {
        this.selectedProposal = selectedProposal;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
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
        private Proposal selectedProposal;
        private List<User> participants = new ArrayList<>();
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

        public DinnerEventBuilder selectedProposal(Proposal selectedProposal) {
            this.selectedProposal = selectedProposal;
            return this;
        }

        public DinnerEventBuilder participants(List<User> participants) {
            this.participants = participants;
            return this;
        }

        public DinnerEventBuilder status(EventStatus status) {
            this.status = status;
            return this;
        }

        public DinnerEvent build() {
            return new DinnerEvent(id, title, description, organizer, proposals, deadline, selectedProposal,
                    participants, status);
        }
    }
}
