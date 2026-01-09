package it.ucdm.leisure.dinnerplan.persistence.sql.entity;

import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import it.ucdm.leisure.dinnerplan.model.Proposal;
import it.ucdm.leisure.dinnerplan.model.ProposalDate;
import it.ucdm.leisure.dinnerplan.model.ProposalRating;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "proposals")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ProposalSqlEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dinner_event_id", nullable = false)
    private DinnerEventSqlEntity dinnerEvent;

    private String location;

    private String address;

    private String description;

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProposalDateSqlEntity> dates = new ArrayList<>();

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProposalRatingSqlEntity> ratings = new HashSet<>();

    public ProposalSqlEntity() {
    }

    public ProposalSqlEntity(Long id, DinnerEventSqlEntity dinnerEvent, String location, String address,
            String description, List<ProposalDateSqlEntity> dates, Set<ProposalRatingSqlEntity> ratings) {
        this.id = id;
        this.dinnerEvent = dinnerEvent;
        this.location = location;
        this.address = address;
        this.description = description;
        this.dates = dates != null ? dates : new ArrayList<>();
        this.ratings = ratings != null ? ratings : new HashSet<>();
    }

    public Proposal toDomain() {
        List<ProposalDate> domainDates = dates.stream().map(ProposalDateSqlEntity::toDomain)
                .collect(Collectors.toList());
        Set<ProposalRating> domainRatings = ratings.stream().map(ProposalRatingSqlEntity::toDomain)
                .collect(Collectors.toSet());

        // Shallow mapping for DinnerEvent to avoid recursion
        DinnerEvent partialEvent = null;
        if (dinnerEvent != null) {
            partialEvent = new DinnerEvent();
            partialEvent.setId(dinnerEvent.getId());
            // We can map basic fields if needed, but definitely NOT proposals list
        }

        return new Proposal(id, location, address, description, domainDates, domainRatings, partialEvent);
    }

    public static ProposalSqlEntity fromDomain(Proposal proposal) {
        ProposalSqlEntity entity = new ProposalSqlEntity();
        entity.setId(proposal.getId());
        entity.setLocation(proposal.getLocation());
        entity.setAddress(proposal.getAddress());
        entity.setDescription(proposal.getDescription());

        // We do not set dinnerEvent here to avoid circular complexity/detachment.
        // Usually the Proposal is added to an Event entity, and the Event entity is
        // saved, cascading the save.
        // If we need to set it, we need the DinnerEventSqlEntity reference.
        // If Domain has it, we could try to look it up or map it, but it's tricky
        // without a Session.

        if (proposal.getDates() != null) {
            entity.setDates(proposal.getDates().stream()
                    .map(d -> {
                        ProposalDateSqlEntity de = ProposalDateSqlEntity.fromDomain(d);
                        de.setProposal(entity);
                        return de;
                    }).collect(Collectors.toList()));
        }

        if (proposal.getRatings() != null) {
            entity.setRatings(proposal.getRatings().stream()
                    .map(r -> {
                        ProposalRatingSqlEntity re = ProposalRatingSqlEntity.fromDomain(r);
                        re.setProposal(entity);
                        return re;
                    }).collect(Collectors.toSet()));
        }
        return entity;
    }

    public static ProposalBuilder builder() {
        return new ProposalBuilder();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DinnerEventSqlEntity getDinnerEvent() {
        return dinnerEvent;
    }

    public void setDinnerEvent(DinnerEventSqlEntity dinnerEvent) {
        this.dinnerEvent = dinnerEvent;
    }

    public List<ProposalDateSqlEntity> getDates() {
        return dates;
    }

    public void setDates(List<ProposalDateSqlEntity> dates) {
        this.dates = dates;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<ProposalRatingSqlEntity> getRatings() {
        return ratings;
    }

    public void setRatings(Set<ProposalRatingSqlEntity> ratings) {
        this.ratings = ratings;
    }

    public static class ProposalBuilder {
        private Long id;
        private DinnerEventSqlEntity dinnerEvent;
        private String location;
        private String address;
        private String description;
        private List<ProposalDateSqlEntity> dates = new ArrayList<>();
        private Set<ProposalRatingSqlEntity> ratings = new HashSet<>();

        public ProposalBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ProposalBuilder dinnerEvent(DinnerEventSqlEntity dinnerEvent) {
            this.dinnerEvent = dinnerEvent;
            return this;
        }

        public ProposalBuilder dates(List<ProposalDateSqlEntity> dates) {
            this.dates = dates;
            return this;
        }

        public ProposalBuilder location(String location) {
            this.location = location;
            return this;
        }

        public ProposalBuilder address(String address) {
            this.address = address;
            return this;
        }

        public ProposalBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ProposalBuilder ratings(Set<ProposalRatingSqlEntity> ratings) {
            this.ratings = ratings;
            return this;
        }

        public ProposalSqlEntity build() {
            return new ProposalSqlEntity(id, dinnerEvent, location, address, description, dates, ratings);
        }
    }
}
