package it.ucdm.leisure.dinnerplan.dto.backup;

import it.ucdm.leisure.dinnerplan.features.user.DietaryPreference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalBackupDTO {
    private Long id;
    private java.util.List<Long> dinnerEventIds;
    private String location;
    private String address;
    private Double latitude;
    private Double longitude;
    private String description;
    private String email;
    private String phoneNumber;
    private String website;
    private Set<DietaryPreference> dietaryPreferences;
}
