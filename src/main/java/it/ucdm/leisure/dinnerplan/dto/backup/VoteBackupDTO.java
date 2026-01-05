package it.ucdm.leisure.dinnerplan.dto.backup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteBackupDTO {
    private Long id;
    private Long userId;
    private Long proposalDateId;
}
