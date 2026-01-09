package it.ucdm.leisure.dinnerplan.persistence.sql;

import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import it.ucdm.leisure.dinnerplan.persistence.sql.entity.DinnerEventSqlEntity;
import it.ucdm.leisure.dinnerplan.persistence.sql.entity.UserSqlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JpaDinnerEventRepository extends JpaRepository<DinnerEventSqlEntity, Long> {

    List<DinnerEventSqlEntity> findDistinctByOrganizerOrParticipantsContainsOrderByDeadlineDesc(UserSqlEntity organizer,
            UserSqlEntity participant);

    List<DinnerEventSqlEntity> findByDeadlineBeforeAndStatus(LocalDateTime deadline, DinnerEvent.EventStatus status);
}
