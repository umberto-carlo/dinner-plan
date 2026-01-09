package it.ucdm.leisure.dinnerplan.persistence.mongo.document;

import it.ucdm.leisure.dinnerplan.model.Proposal;
import it.ucdm.leisure.dinnerplan.model.ProposalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Document(collection = "proposals")
public class ProposalDocument {

    @Id
    private Long id;
    private Long dinnerEventId;
    private String location;
    private String address;
    private String description;
    private List<ProposalDateDocument> dates = new ArrayList<>();

    public ProposalDocument() {
    }

    public ProposalDocument(Long id, Long dinnerEventId, String location, String address, String description,
            List<ProposalDateDocument> dates) {
        this.id = id;
        this.dinnerEventId = dinnerEventId;
        this.location = location;
        this.address = address;
        this.description = description;
        this.dates = dates != null ? dates : new ArrayList<>();
    }

    public Proposal toDomain() {
        return new Proposal(
                id,
                location,
                address,
                description,
                dates.stream().map(ProposalDateDocument::toDomain).collect(Collectors.toList()),
                null, // Ratings are fetched separately or mapped via adapter
                null // DinnerEvent is set by adapter
        );
    }

    public static ProposalDocument fromDomain(Proposal proposal) {
        return new ProposalDocument(
                proposal.getId(),
                proposal.getDinnerEvent() != null ? proposal.getDinnerEvent().getId() : null,
                proposal.getLocation(),
                proposal.getAddress(),
                proposal.getDescription(),
                proposal.getDates() != null
                        ? proposal.getDates().stream().map(ProposalDateDocument::fromDomain)
                                .collect(Collectors.toList())
                        : new ArrayList<>());
    }

    public static class ProposalDateDocument {
        private Long id;
        private LocalDateTime date;

        public ProposalDateDocument() {
        }

        public ProposalDateDocument(Long id, LocalDateTime date) {
            this.id = id;
            this.date = date;
        }

        public ProposalDate toDomain() {
            return new ProposalDate(id, date, new ArrayList<>());
        }

        public static ProposalDateDocument fromDomain(ProposalDate date) {
            return new ProposalDateDocument(date.getId(), date.getDate());
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
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDinnerEventId() {
        return dinnerEventId;
    }

    public void setDinnerEventId(Long dinnerEventId) {
        this.dinnerEventId = dinnerEventId;
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

    public List<ProposalDateDocument> getDates() {
        return dates;
    }

    public void setDates(List<ProposalDateDocument> dates) {
        this.dates = dates;
    }
}
