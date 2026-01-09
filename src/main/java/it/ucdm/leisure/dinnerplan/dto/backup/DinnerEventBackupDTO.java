package it.ucdm.leisure.dinnerplan.dto.backup;

import it.ucdm.leisure.dinnerplan.model.DinnerEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class DinnerEventBackupDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private DinnerEvent.EventStatus status;
    private String organizer;
    private List<String> participants;
    private Set<Long> proposalIds;

    private Long selectedProposalDateId;

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

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public Set<Long> getProposalIds() {
        return proposalIds;
    }

    public void setProposalIds(Set<Long> proposalIds) {
        this.proposalIds = proposalIds;
    }

    public Long getSelectedProposalDateId() {
        return selectedProposalDateId;
    }

    public void setSelectedProposalDateId(Long selectedProposalDateId) {
        this.selectedProposalDateId = selectedProposalDateId;
    }
}
