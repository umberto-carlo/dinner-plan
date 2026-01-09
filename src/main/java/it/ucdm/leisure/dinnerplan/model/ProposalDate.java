package it.ucdm.leisure.dinnerplan.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProposalDate {
    private Long id;
    private LocalDateTime date;
    private List<Vote> votes = new ArrayList<>();
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Proposal proposal;
    // Back reference to Proposal? Usually not needed in Domain if accessed via
    // Proposal.

    public ProposalDate() {
    }

    public ProposalDate(Long id, LocalDateTime date, List<Vote> votes) {
        this.id = id;
        this.date = date;
        this.votes = votes != null ? votes : new ArrayList<>();
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

    public List<Vote> getVotes() {
        return votes;
    }

    public void setVotes(List<Vote> votes) {
        this.votes = votes;
    }

    public Proposal getProposal() {
        return proposal;
    }

    public void setProposal(Proposal proposal) {
        this.proposal = proposal;
    }
}
