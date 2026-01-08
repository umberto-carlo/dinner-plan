package it.ucdm.leisure.dinnerplan.features.proposal;

import it.ucdm.leisure.dinnerplan.features.user.User;

import jakarta.persistence.*;

@Entity
@Table(name = "votes", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "proposal_date_id" })
})
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_date_id", nullable = false)
    private ProposalDate proposalDate;

    public Vote() {
    }

    public Vote(Long id, User user, ProposalDate proposalDate) {
        this.id = id;
        this.user = user;
        this.proposalDate = proposalDate;
    }

    public static VoteBuilder builder() {
        return new VoteBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public ProposalDate getProposalDate() {
        return proposalDate;
    }

    public void setProposalDate(ProposalDate proposalDate) {
        this.proposalDate = proposalDate;
    }

    public static class VoteBuilder {
        private Long id;
        private User user;
        private ProposalDate proposalDate;

        public VoteBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public VoteBuilder user(User user) {
            this.user = user;
            return this;
        }

        public VoteBuilder proposalDate(ProposalDate proposalDate) {
            this.proposalDate = proposalDate;
            return this;
        }

        public Vote build() {
            return new Vote(id, user, proposalDate);
        }
    }
}
