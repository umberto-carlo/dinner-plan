package it.ucdm.leisure.dinnerplan.features.proposal;

import it.ucdm.leisure.dinnerplan.features.event.DinnerEvent;
import it.ucdm.leisure.dinnerplan.features.user.DietaryPreference;
import it.ucdm.leisure.dinnerplan.features.user.User;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "proposals")
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @ManyToMany(mappedBy = "proposals")
    private List<DinnerEvent> dinnerEvents = new ArrayList<>();

    private String location;

    private String address;

    @Column(nullable = true)
    private Double latitude;

    @Column(nullable = true)
    private Double longitude;

    private String description;

    @ElementCollection(targetClass = DietaryPreference.class)
    @CollectionTable(name = "proposal_dietary_preferences", joinColumns = @JoinColumn(name = "proposal_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "dietary_preference")
    private Set<DietaryPreference> dietaryPreferences = new HashSet<>();

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProposalDate> dates = new ArrayList<>();

    @OneToMany(mappedBy = "proposal", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProposalRating> ratings = new HashSet<>();

    public Proposal() {
    }

    public Proposal(Long id, List<DinnerEvent> dinnerEvents, String location, String address, Double latitude, Double longitude,
            String description, List<ProposalDate> dates, Set<ProposalRating> ratings, Set<DietaryPreference> dietaryPreferences) {
        this.id = id;
        this.dinnerEvents = dinnerEvents != null ? dinnerEvents : new ArrayList<>();
        this.location = location;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.dates = dates != null ? dates : new ArrayList<>();
        this.ratings = ratings != null ? ratings : new HashSet<>();
        this.dietaryPreferences = dietaryPreferences != null ? dietaryPreferences : new HashSet<>();
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

    @com.fasterxml.jackson.annotation.JsonIgnore
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
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

    public Set<DietaryPreference> getDietaryPreferences() {
        return dietaryPreferences;
    }

    public void setDietaryPreferences(Set<DietaryPreference> dietaryPreferences) {
        this.dietaryPreferences = dietaryPreferences;
    }

    public boolean hasVotes() {
        return dates != null && dates.stream().anyMatch(d -> d.getVotes() != null && !d.getVotes().isEmpty());
    }

    public List<User> getIncompatibleParticipants(DinnerEvent event) {
        if (event == null || event.getParticipants() == null) {
            return new ArrayList<>();
        }
        return event.getParticipants().stream()
                .filter(p -> p.getDietaryPreference() != DietaryPreference.OMNIVORE && !dietaryPreferences.contains(p.getDietaryPreference()))
                .collect(Collectors.toList());
    }

    public static class ProposalBuilder {
        private Long id;
        private List<DinnerEvent> dinnerEvents = new ArrayList<>();
        private String location;
        private String address;
        private Double latitude;
        private Double longitude;
        private String description;
        private List<ProposalDate> dates = new ArrayList<>();
        private Set<ProposalRating> ratings = new HashSet<>();
        private Set<DietaryPreference> dietaryPreferences = new HashSet<>();

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

        public ProposalBuilder latitude(Double latitude) {
            this.latitude = latitude;
            return this;
        }

        public ProposalBuilder longitude(Double longitude) {
            this.longitude = longitude;
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

        public ProposalBuilder dietaryPreferences(Set<DietaryPreference> dietaryPreferences) {
            this.dietaryPreferences = dietaryPreferences;
            return this;
        }

        public Proposal build() {
            return new Proposal(id, dinnerEvents, location, address, latitude, longitude, description, dates, ratings, dietaryPreferences);
        }
    }
}
