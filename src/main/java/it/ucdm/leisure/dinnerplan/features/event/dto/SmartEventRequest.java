package it.ucdm.leisure.dinnerplan.features.event.dto;

import java.util.List;

public class SmartEventRequest {
    private String title;
    private String description;
    private String deadline;
    private List<NewProposalDTO> proposals;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public List<NewProposalDTO> getProposals() {
        return proposals;
    }

    public void setProposals(List<NewProposalDTO> proposals) {
        this.proposals = proposals;
    }

    public static class NewProposalDTO {
        private String location;
        private String address;
        private String description;
        private String dateOption;

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

        public String getDateOption() {
            return dateOption;
        }

        public void setDateOption(String dateOption) {
            this.dateOption = dateOption;
        }
    }
}
