package it.ucdm.leisure.dinnerplan.persistence.mongo.document;

import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Document(collection = "dinner_events")
public class DinnerEventDocument {

    @Id
    private Long id;
    private String title;
    private String description;
    private UserDocument organizer;
    private LocalDateTime deadline;
    private DinnerEvent.EventStatus status;
    private List<Long> participantIds = new ArrayList<>();
    private Long selectedProposalDateId;

    public DinnerEventDocument() {
    }

    public DinnerEventDocument(Long id, String title, String description, UserDocument organizer,
            LocalDateTime deadline, DinnerEvent.EventStatus status,
            List<Long> participantIds, Long selectedProposalDateId) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.organizer = organizer;
        this.deadline = deadline;
        this.status = status;
        this.participantIds = participantIds != null ? participantIds : new ArrayList<>();
        this.selectedProposalDateId = selectedProposalDateId;
    }

    public DinnerEvent toDomain() {
        return DinnerEvent.builder()
                .id(id)
                .title(title)
                .description(description)
                .organizer(organizer != null ? organizer.toDomain() : null)
                .deadline(deadline)
                .status(status)
                .build();
    }

    public static DinnerEventDocument fromDomain(DinnerEvent event) {
        return new DinnerEventDocument(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getOrganizer() != null ? UserDocument.fromDomain(event.getOrganizer()) : null,
                event.getDeadline(),
                event.getStatus(),
                event.getParticipants() != null
                        ? event.getParticipants().stream().map(u -> u.getId()).collect(Collectors.toList())
                        : new ArrayList<>(),
                event.getSelectedProposalDate() != null ? event.getSelectedProposalDate().getId() : null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public UserDocument getOrganizer() {
        return organizer;
    }

    public void setOrganizer(UserDocument organizer) {
        this.organizer = organizer;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public DinnerEvent.EventStatus getStatus() {
        return status;
    }

    public void setStatus(DinnerEvent.EventStatus status) {
        this.status = status;
    }

    public List<Long> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<Long> participantIds) {
        this.participantIds = participantIds;
    }

    public Long getSelectedProposalDateId() {
        return selectedProposalDateId;
    }

    public void setSelectedProposalDateId(Long selectedProposalDateId) {
        this.selectedProposalDateId = selectedProposalDateId;
    }
}
