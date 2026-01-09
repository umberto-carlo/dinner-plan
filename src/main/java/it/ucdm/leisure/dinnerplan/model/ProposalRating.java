package it.ucdm.leisure.dinnerplan.model;

public class ProposalRating {
    private Long id;
    private boolean isLiked;
    private User user;
    private Proposal proposal;

    public ProposalRating() {
    }

    public ProposalRating(Long id, boolean isLiked, User user, Proposal proposal) {
        this.id = id;
        this.isLiked = isLiked;
        this.user = user;
        this.proposal = proposal;
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

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Proposal getProposal() {
        return proposal;
    }

    public void setProposal(Proposal proposal) {
        this.proposal = proposal;
    }

    public static class ProposalRatingBuilder {
        private Long id;
        private boolean isLiked;
        private User user;
        private Proposal proposal;

        public ProposalRatingBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ProposalRatingBuilder isLiked(boolean isLiked) {
            this.isLiked = isLiked;
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

        public ProposalRating build() {
            return new ProposalRating(id, isLiked, user, proposal);
        }
    }
}
