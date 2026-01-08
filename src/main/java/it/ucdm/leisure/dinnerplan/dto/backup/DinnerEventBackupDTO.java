package it.ucdm.leisure.dinnerplan.dto.backup;

import it.ucdm.leisure.dinnerplan.features.event.DinnerEvent.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DinnerEventBackupDTO {
    private Long id;
    private String title;
    private String description;
    private Long organizerId;
    private LocalDateTime deadline;
    private Long selectedProposalDateId;
    private EventStatus status;
    private List<Long> participantIds;
}
