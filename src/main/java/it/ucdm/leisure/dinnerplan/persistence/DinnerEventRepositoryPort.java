package it.ucdm.leisure.dinnerplan.persistence;

import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import it.ucdm.leisure.dinnerplan.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DinnerEventRepositoryPort {
    List<DinnerEvent> findDistinctByOrganizerOrParticipantsContainsOrderByDeadlineDesc(User organizer,
            User participant);

    List<DinnerEvent> findByDeadlineBeforeAndStatus(LocalDateTime deadline, DinnerEvent.EventStatus status);

    Optional<DinnerEvent> findById(Long id);

    DinnerEvent save(DinnerEvent event);

    void deleteById(Long id);

    List<DinnerEvent> findAll();
}
