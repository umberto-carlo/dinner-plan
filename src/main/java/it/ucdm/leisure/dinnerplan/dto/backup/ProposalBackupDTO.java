package it.ucdm.leisure.dinnerplan.dto.backup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalBackupDTO {
    private Long id;
    private java.util.List<Long> dinnerEventIds;
    private String location;
    private String address;
    private String description;
}
