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
public class DinnerEventMessageBackupDTO {
    private Long id;
    private Long eventId;
    private Long senderId;
    private String content;
    private LocalDateTime timestamp;
}
