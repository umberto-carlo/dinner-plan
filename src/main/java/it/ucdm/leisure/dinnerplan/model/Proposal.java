package it.ucdm.leisure.dinnerplan.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dinner_event_id", nullable = false)
    private DinnerEvent dinnerEvent;

    private LocalDateTime dateOption;

    private String location;

    private String address;

    private String description;

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Vote> votes = new HashSet<>();

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProposalRating> ratings = new HashSet<>();

    public Proposal() {
    }

    public Proposal(Long id, DinnerEvent dinnerEvent, LocalDateTime dateOption, String location, String address,
            String description, Set<Vote> votes, Set<ProposalRating> ratings) {
        this.id = id;
        this.dinnerEvent = dinnerEvent;
        this.dateOption = dateOption;
        this.location = location;
        this.address = address;
        this.description = description;
        this.votes = votes != null ? votes : new HashSet<>();
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

    public DinnerEvent getDinnerEvent() {
        return dinnerEvent;
    }

    public void setDinnerEvent(DinnerEvent dinnerEvent) {
        this.dinnerEvent = dinnerEvent;
    }

    public LocalDateTime getDateOption() {
        return dateOption;
    }

    public void setDateOption(LocalDateTime dateOption) {
        this.dateOption = dateOption;
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

    public Set<Vote> getVotes() {
        return votes;
    }

    public void setVotes(Set<Vote> votes) {
        this.votes = votes;
    }

    public Set<ProposalRating> getRatings() {
        return ratings;
    }

    public void setRatings(Set<ProposalRating> ratings) {
        this.ratings = ratings;
    }

    public static class ProposalBuilder {
        private Long id;
        private DinnerEvent dinnerEvent;
        private LocalDateTime dateOption;
        private String location;
        private String address;
        private String description;
        private Set<Vote> votes = new HashSet<>();
        private Set<ProposalRating> ratings = new HashSet<>();

        public ProposalBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ProposalBuilder dinnerEvent(DinnerEvent dinnerEvent) {
            this.dinnerEvent = dinnerEvent;
            return this;
        }

        public ProposalBuilder dateOption(LocalDateTime dateOption) {
            this.dateOption = dateOption;
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

        public ProposalBuilder votes(Set<Vote> votes) {
            this.votes = votes;
            return this;
        }

        public ProposalBuilder ratings(Set<ProposalRating> ratings) {
            this.ratings = ratings;
            return this;
        }

        public Proposal build() {
            return new Proposal(id, dinnerEvent, dateOption, location, address, description, votes, ratings);
        }
    }
}
