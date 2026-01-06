package it.ucdm.leisure.dinnerplan.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "proposal_dates")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ProposalDate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dinner_event_id", nullable = false)
    private DinnerEvent dinnerEvent;

    @OneToMany(mappedBy = "proposalDate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Vote> votes = new HashSet<>();

    public ProposalDate() {
    }

    public ProposalDate(Long id, LocalDateTime date, Proposal proposal, DinnerEvent dinnerEvent, Set<Vote> votes) {
        this.id = id;
        this.date = date;
        this.proposal = proposal;
        this.dinnerEvent = dinnerEvent;
        this.votes = votes != null ? votes : new HashSet<>();
    }

    public static ProposalDateBuilder builder() {
        return new ProposalDateBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public Proposal getProposal() {
        return proposal;
    }

    public void setProposal(Proposal proposal) {
        this.proposal = proposal;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DinnerEvent getDinnerEvent() {
        return dinnerEvent;
    }

    public void setDinnerEvent(DinnerEvent dinnerEvent) {
        this.dinnerEvent = dinnerEvent;
    }

    public Set<Vote> getVotes() {
        return votes;
    }

    public void setVotes(Set<Vote> votes) {
        this.votes = votes;
    }

    public static class ProposalDateBuilder {
        private Long id;
        private LocalDateTime date;
        private Proposal proposal;
        private DinnerEvent dinnerEvent;
        private Set<Vote> votes = new HashSet<>();

        public ProposalDateBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ProposalDateBuilder date(LocalDateTime date) {
            this.date = date;
            return this;
        }

        public ProposalDateBuilder proposal(Proposal proposal) {
            this.proposal = proposal;
            return this;
        }

        public ProposalDateBuilder dinnerEvent(DinnerEvent dinnerEvent) {
            this.dinnerEvent = dinnerEvent;
            return this;
        }

        public ProposalDateBuilder votes(Set<Vote> votes) {
            this.votes = votes;
            return this;
        }

        public ProposalDate build() {
            return new ProposalDate(id, date, proposal, dinnerEvent, votes);
        }
    }
}
