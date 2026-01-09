package it.ucdm.leisure.dinnerplan.persistence.sql.entity;

import it.ucdm.leisure.dinnerplan.model.ProposalDate;
import it.ucdm.leisure.dinnerplan.model.Vote;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "proposal_dates")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ProposalDateSqlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private ProposalSqlEntity proposal;

    @OneToMany(mappedBy = "proposalDate", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VoteSqlEntity> votes = new HashSet<>();

    public ProposalDateSqlEntity() {
    }

    public ProposalDateSqlEntity(Long id, LocalDateTime date, ProposalSqlEntity proposal,
            Set<VoteSqlEntity> votes) {
        this.id = id;
        this.date = date;
        this.proposal = proposal;
        this.votes = votes != null ? votes : new HashSet<>();
    }

    public ProposalDate toDomain() {
        List<Vote> domainVotes = votes.stream().map(VoteSqlEntity::toDomain).collect(Collectors.toList());
        return new ProposalDate(id, date, domainVotes);
    }

    public static ProposalDateSqlEntity fromDomain(ProposalDate date) {
        ProposalDateSqlEntity entity = new ProposalDateSqlEntity();
        entity.setId(date.getId());
        entity.setDate(date.getDate());
        if (date.getVotes() != null) {
            entity.setVotes(date.getVotes().stream()
                    .map(v -> {
                        VoteSqlEntity ve = VoteSqlEntity.fromDomain(v);
                        ve.setProposalDate(entity);
                        return ve;
                    }).collect(Collectors.toSet()));
        }
        return entity;
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
    public ProposalSqlEntity getProposal() {
        return proposal;
    }

    public void setProposal(ProposalSqlEntity proposal) {
        this.proposal = proposal;
    }

    public Set<VoteSqlEntity> getVotes() {
        return votes;
    }

    public void setVotes(Set<VoteSqlEntity> votes) {
        this.votes = votes;
    }

    public static class ProposalDateBuilder {
        private Long id;
        private LocalDateTime date;
        private ProposalSqlEntity proposal;
        private Set<VoteSqlEntity> votes = new HashSet<>();

        public ProposalDateBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ProposalDateBuilder date(LocalDateTime date) {
            this.date = date;
            return this;
        }

        public ProposalDateBuilder proposal(ProposalSqlEntity proposal) {
            this.proposal = proposal;
            return this;
        }

        public ProposalDateBuilder votes(Set<VoteSqlEntity> votes) {
            this.votes = votes;
            return this;
        }

        public ProposalDateSqlEntity build() {
            return new ProposalDateSqlEntity(id, date, proposal, votes);
        }
    }
}
