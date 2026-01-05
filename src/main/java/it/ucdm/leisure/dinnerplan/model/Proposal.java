package it.ucdm.leisure.dinnerplan.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "proposals")
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(mappedBy = "proposals")
    private List<DinnerEvent> dinnerEvents = new ArrayList<>();

    private String location;

    private String address;

    private String description;

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProposalDate> dates = new ArrayList<>();

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProposalRating> ratings = new HashSet<>();

    public Proposal() {
    }

    public Proposal(Long id, List<DinnerEvent> dinnerEvents, String location, String address,
            String description, List<ProposalDate> dates, Set<ProposalRating> ratings) {
        this.id = id;
        this.dinnerEvents = dinnerEvents != null ? dinnerEvents : new ArrayList<>();
        this.location = location;
        this.address = address;
        this.description = description;
        this.dates = dates != null ? dates : new ArrayList<>();
        this.ratings = ratings != null ? ratings : new HashSet<>();
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

    public List<DinnerEvent> getDinnerEvents() {
        return dinnerEvents;
    }

    public void setDinnerEvents(List<DinnerEvent> dinnerEvents) {
        this.dinnerEvents = dinnerEvents;
    }

    public List<ProposalDate> getDates() {
        return dates;
    }

    public void setDates(List<ProposalDate> dates) {
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

    public Set<ProposalRating> getRatings() {
        return ratings;
    }

    public void setRatings(Set<ProposalRating> ratings) {
        this.ratings = ratings;
    }

    public boolean hasVotes() {
        return dates != null && dates.stream().anyMatch(d -> d.getVotes() != null && !d.getVotes().isEmpty());
    }

    public static class ProposalBuilder {
        private Long id;
        private List<DinnerEvent> dinnerEvents = new ArrayList<>();
        private String location;
        private String address;
        private String description;
        private List<ProposalDate> dates = new ArrayList<>();
        private Set<ProposalRating> ratings = new HashSet<>();

        public ProposalBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ProposalBuilder dinnerEvents(List<DinnerEvent> dinnerEvents) {
            this.dinnerEvents = dinnerEvents;
            return this;
        }

        public ProposalBuilder dates(List<ProposalDate> dates) {
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

        public ProposalBuilder ratings(Set<ProposalRating> ratings) {
            this.ratings = ratings;
            return this;
        }

        public Proposal build() {
            return new Proposal(id, dinnerEvents, location, address, description, dates, ratings);
        }
    }
}
