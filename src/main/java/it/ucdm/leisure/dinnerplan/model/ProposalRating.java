package it.ucdm.leisure.dinnerplan.model;

import jakarta.persistence.*;

@Entity
@Table(name = "proposal_ratings", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "proposal_id" })
})
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ProposalRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private Proposal proposal;

    @Column(nullable = false)
    private boolean isLiked;

    public ProposalRating() {
    }

    public ProposalRating(Long id, User user, Proposal proposal, boolean isLiked) {
        this.id = id;
        this.user = user;
        this.proposal = proposal;
        this.isLiked = isLiked;
    }

    public static ProposalRatingBuilder builder() {
        return new ProposalRatingBuilder();
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
    public Proposal getProposal() {
        return proposal;
    }

    public void setProposal(Proposal proposal) {
        this.proposal = proposal;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public static class ProposalRatingBuilder {
        private Long id;
        private User user;
        private Proposal proposal;
        private boolean isLiked;

        public ProposalRatingBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ProposalRatingBuilder user(User user) {
            this.user = user;
            return this;
        }

        public ProposalRatingBuilder proposal(Proposal proposal) {
            this.proposal = proposal;
            return this;
        }

        public ProposalRatingBuilder isLiked(boolean isLiked) {
            this.isLiked = isLiked;
            return this;
        }

        public ProposalRating build() {
            return new ProposalRating(id, user, proposal, isLiked);
        }
    }
}
