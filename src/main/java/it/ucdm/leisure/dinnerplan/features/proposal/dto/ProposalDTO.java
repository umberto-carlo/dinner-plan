package it.ucdm.leisure.dinnerplan.features.proposal.dto;

import it.ucdm.leisure.dinnerplan.features.proposal.Proposal;
import it.ucdm.leisure.dinnerplan.features.proposal.ProposalDate;
import it.ucdm.leisure.dinnerplan.features.proposal.ProposalRating;
import it.ucdm.leisure.dinnerplan.features.user.DietaryPreference;

import java.util.List;
import java.util.Set;

public class ProposalDTO {

    private final Proposal proposal;
    private Double distanceFromUser; // in km

    public ProposalDTO(Proposal proposal) {
        this.proposal = proposal;
    }

    public Proposal getProposal() {
        return proposal;
    }

    public Double getDistanceFromUser() {
        return distanceFromUser;
    }

    public void setDistanceFromUser(Double distanceFromUser) {
        this.distanceFromUser = distanceFromUser;
    }

    // Delegate methods to make it easy to use in Thymeleaf
    public Long getId() {
        return proposal.getId();
    }

    public String getLocation() {
        return proposal.getLocation();
    }

    public String getAddress() {
        return proposal.getAddress();
    }

    public String getDescription() {
        return proposal.getDescription();
    }

    public Set<DietaryPreference> getDietaryPreferences() {
        return proposal.getDietaryPreferences();
    }

    public List<ProposalDate> getDates() {
        return proposal.getDates();
    }

    public Set<ProposalRating> getRatings() {
        return proposal.getRatings();
    }
    
    public boolean hasVotes() {
        return proposal.hasVotes();
    }
}
