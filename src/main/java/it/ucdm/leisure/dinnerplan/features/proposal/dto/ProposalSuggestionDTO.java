package it.ucdm.leisure.dinnerplan.features.proposal.dto;

import it.ucdm.leisure.dinnerplan.features.user.DietaryPreference;

import java.util.HashSet;
import java.util.Set;

public class ProposalSuggestionDTO {
    private String location;
    private String address;
    private String description;
    private long totalLikes;
    private long totalDislikes;
    private int usageCount;
    private Set<DietaryPreference> dietaryPreferences = new HashSet<>();

    private String encodedData;

    public ProposalSuggestionDTO() {
    }

    public ProposalSuggestionDTO(String location, String address, String description, long totalLikes,
            long totalDislikes, int usageCount, Set<DietaryPreference> dietaryPreferences) {
        this.location = location;
        this.address = address;
        this.description = description;
        this.totalLikes = totalLikes;
        this.totalDislikes = totalDislikes;
        this.usageCount = usageCount;
        this.dietaryPreferences = dietaryPreferences != null ? dietaryPreferences : new HashSet<>();
    }

    public static ProposalSuggestionDTOBuilder builder() {
        return new ProposalSuggestionDTOBuilder();
    }

    public String getEncodedData() {
        return encodedData;
    }

    public void setEncodedData(String encodedData) {
        this.encodedData = encodedData;
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

    public long getTotalLikes() {
        return totalLikes;
    }

    public void setTotalLikes(long totalLikes) {
        this.totalLikes = totalLikes;
    }

    public long getTotalDislikes() {
        return totalDislikes;
    }

    public void setTotalDislikes(long totalDislikes) {
        this.totalDislikes = totalDislikes;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    public Set<DietaryPreference> getDietaryPreferences() {
        return dietaryPreferences;
    }

    public void setDietaryPreferences(Set<DietaryPreference> dietaryPreferences) {
        this.dietaryPreferences = dietaryPreferences;
    }

    // Builder class update
    public static class ProposalSuggestionDTOBuilder {
        private String location;
        private String address;
        private String description;
        private long totalLikes;
        private long totalDislikes;
        private int usageCount;
        private Set<DietaryPreference> dietaryPreferences = new HashSet<>();
        private String encodedData;

        public ProposalSuggestionDTOBuilder location(String location) {
            this.location = location;
            return this;
        }

        public ProposalSuggestionDTOBuilder address(String address) {
            this.address = address;
            return this;
        }

        public ProposalSuggestionDTOBuilder description(String description) {
            this.description = description;
            return this;
        }

        public ProposalSuggestionDTOBuilder totalLikes(long totalLikes) {
            this.totalLikes = totalLikes;
            return this;
        }

        public ProposalSuggestionDTOBuilder totalDislikes(long totalDislikes) {
            this.totalDislikes = totalDislikes;
            return this;
        }

        public ProposalSuggestionDTOBuilder usageCount(int usageCount) {
            this.usageCount = usageCount;
            return this;
        }

        public ProposalSuggestionDTOBuilder dietaryPreferences(Set<DietaryPreference> dietaryPreferences) {
            this.dietaryPreferences = dietaryPreferences;
            return this;
        }

        public ProposalSuggestionDTOBuilder encodedData(String encodedData) {
            this.encodedData = encodedData;
            return this;
        }

        public ProposalSuggestionDTO build() {
            ProposalSuggestionDTO dto = new ProposalSuggestionDTO(location, address, description, totalLikes,
                    totalDislikes, usageCount, dietaryPreferences);
            dto.setEncodedData(encodedData);
            return dto;
        }
    }
}
