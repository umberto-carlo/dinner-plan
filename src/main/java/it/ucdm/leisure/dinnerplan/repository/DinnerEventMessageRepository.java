package it.ucdm.leisure.dinnerplan.repository;

import it.ucdm.leisure.dinnerplan.model.DinnerEventMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DinnerEventMessageRepository extends JpaRepository<DinnerEventMessage, Long> {
    List<DinnerEventMessage> findByEventIdOrderByTimestampAsc(Long eventId);
}
