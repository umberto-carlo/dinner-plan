package it.ucdm.leisure.dinnerplan.persistence.sql.entity;

import it.ucdm.leisure.dinnerplan.model.ProposalRating;

import jakarta.persistence.*;

@Entity
@Table(name = "proposal_ratings")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ProposalRatingSqlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean isLiked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserSqlEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private ProposalSqlEntity proposal;

    public ProposalRatingSqlEntity() {
    }

    public ProposalRatingSqlEntity(Long id, boolean isLiked, UserSqlEntity user, ProposalSqlEntity proposal) {
        this.id = id;
        this.isLiked = isLiked;
        this.user = user;
        this.proposal = proposal;
    }

    public ProposalRating toDomain() {
        return new ProposalRating(id, isLiked, user != null ? user.toDomain() : null,
                proposal != null ? proposal.toDomain() : null);
    }

    public static ProposalRatingSqlEntity fromDomain(ProposalRating rating) {
        ProposalRatingSqlEntity entity = new ProposalRatingSqlEntity();
        entity.setId(rating.getId());
        entity.setLiked(rating.isLiked());
        if (rating.getUser() != null) {
            entity.setUser(UserSqlEntity.fromDomain(rating.getUser()));
        }
        if (rating.getProposal() != null) {
            entity.setProposal(ProposalSqlEntity.fromDomain(rating.getProposal()));
        }
        return entity;
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

    public UserSqlEntity getUser() {
        return user;
    }

    public void setUser(UserSqlEntity user) {
        this.user = user;
    }

    public ProposalSqlEntity getProposal() {
        return proposal;
    }

    public void setProposal(ProposalSqlEntity proposal) {
        this.proposal = proposal;
    }

    public static class ProposalRatingBuilder {
        private Long id;
        private boolean isLiked;
        private UserSqlEntity user;
        private ProposalSqlEntity proposal;

        public ProposalRatingBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ProposalRatingBuilder isLiked(boolean isLiked) {
            this.isLiked = isLiked;
            return this;
        }

        public ProposalRatingBuilder user(UserSqlEntity user) {
            this.user = user;
            return this;
        }

        public ProposalRatingBuilder proposal(ProposalSqlEntity proposal) {
            this.proposal = proposal;
            return this;
        }

        public ProposalRatingSqlEntity build() {
            return new ProposalRatingSqlEntity(id, isLiked, user, proposal);
        }
    }
}
