package it.ucdm.leisure.dinnerplan.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Proposal {
    private Long id;
    private String location;
    private String address;
    private String description;
    private DinnerEvent dinnerEvent;

    private List<ProposalDate> dates = new ArrayList<>();
    private Set<ProposalRating> ratings = new HashSet<>();

    public Proposal() {
    }

    public Proposal(Long id, String location, String address, String description, List<ProposalDate> dates,
            Set<ProposalRating> ratings, DinnerEvent dinnerEvent) {
        this.id = id;
        this.location = location;
        this.address = address;
        this.description = description;
        this.dates = dates != null ? dates : new ArrayList<>();
        this.ratings = ratings != null ? ratings : new HashSet<>();
        this.dinnerEvent = dinnerEvent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<ProposalDate> getDates() {
        return dates;
    }

    public void setDates(List<ProposalDate> dates) {
        this.dates = dates;
    }

    public Set<ProposalRating> getRatings() {
        return ratings;
    }

    public void setRatings(Set<ProposalRating> ratings) {
        this.ratings = ratings;
    }

    @com.fasterxml.jackson.annotation.JsonIgnore
    public DinnerEvent getDinnerEvent() {
        return dinnerEvent;
    }

    public void setDinnerEvent(DinnerEvent dinnerEvent) {
        this.dinnerEvent = dinnerEvent;
    }

    public boolean hasVotes() {
        return dates != null && dates.stream().anyMatch(d -> d.getVotes() != null && !d.getVotes().isEmpty());
    }
}
