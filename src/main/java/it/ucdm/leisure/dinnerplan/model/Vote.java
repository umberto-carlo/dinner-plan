package it.ucdm.leisure.dinnerplan.model;

import lombok.Builder;

public class Vote {
    private Long id;
    private User user;
    private ProposalDate proposalDate;

    public Vote() {
    }

    public Vote(Long id, User user, ProposalDate proposalDate) {
        this.id = id;
        this.user = user;
        this.proposalDate = proposalDate;
    }

    // Builder pattern implementation manually or using Lombok if available
    // Assuming Lombok is available due to .builder() usage in code
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
