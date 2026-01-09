package it.ucdm.leisure.dinnerplan.persistence.sql.entity;

import it.ucdm.leisure.dinnerplan.model.Vote;

import jakarta.persistence.*;

@Entity
@Table(name = "votes")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class VoteSqlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserSqlEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_date_id", nullable = false)
    private ProposalDateSqlEntity proposalDate;

    public VoteSqlEntity() {
    }

    public VoteSqlEntity(Long id, UserSqlEntity user, ProposalDateSqlEntity proposalDate) {
        this.id = id;
        this.user = user;
        this.proposalDate = proposalDate;
    }

    public Vote toDomain() {
        return new Vote(id, user != null ? user.toDomain() : null,
                proposalDate != null ? proposalDate.toDomain() : null);
    }

    public static VoteSqlEntity fromDomain(Vote vote) {
        VoteSqlEntity entity = new VoteSqlEntity();
        entity.setId(vote.getId());
        if (vote.getUser() != null) {
            entity.setUser(UserSqlEntity.fromDomain(vote.getUser()));
        }
        if (vote.getProposalDate() != null) {
            entity.setProposalDate(ProposalDateSqlEntity.fromDomain(vote.getProposalDate()));
        }
        return entity;
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

    public UserSqlEntity getUser() {
        return user;
    }

    public void setUser(UserSqlEntity user) {
        this.user = user;
    }

    public ProposalDateSqlEntity getProposalDate() {
        return proposalDate;
    }

    public void setProposalDate(ProposalDateSqlEntity proposalDate) {
        this.proposalDate = proposalDate;
    }

    public static class VoteBuilder {
        private Long id;
        private UserSqlEntity user;
        private ProposalDateSqlEntity proposalDate;

        public VoteBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public VoteBuilder user(UserSqlEntity user) {
            this.user = user;
            return this;
        }

        public VoteBuilder proposalDate(ProposalDateSqlEntity proposalDate) {
            this.proposalDate = proposalDate;
            return this;
        }

        public VoteSqlEntity build() {
            return new VoteSqlEntity(id, user, proposalDate);
        }
    }
}
