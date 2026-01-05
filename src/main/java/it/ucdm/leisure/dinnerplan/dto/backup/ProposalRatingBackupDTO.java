package it.ucdm.leisure.dinnerplan.dto.backup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalRatingBackupDTO {
    private Long id;
    private Long userId;
    private Long proposalId;
    private boolean isLiked;
}
