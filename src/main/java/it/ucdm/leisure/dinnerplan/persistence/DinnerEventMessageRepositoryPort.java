package it.ucdm.leisure.dinnerplan.persistence;

import it.ucdm.leisure.dinnerplan.model.DinnerEventMessage;
import java.util.List;

public interface DinnerEventMessageRepositoryPort {
    DinnerEventMessage save(DinnerEventMessage message);

    List<DinnerEventMessage> findByEventIdOrderByTimestampAsc(Long eventId);
}
