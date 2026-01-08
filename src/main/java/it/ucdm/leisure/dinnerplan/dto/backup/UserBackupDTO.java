package it.ucdm.leisure.dinnerplan.dto.backup;

import it.ucdm.leisure.dinnerplan.features.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBackupDTO {
    private Long id;
    private String username;
    private String password;
    private Role role;
}
