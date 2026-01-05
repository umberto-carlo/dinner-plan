package it.ucdm.leisure.dinnerplan.dto.backup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalDateBackupDTO {
    private Long id;
    private LocalDateTime date;
    private Long proposalId;
    private Long dinnerEventId;
}
