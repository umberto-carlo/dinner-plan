package it.ucdm.leisure.dinnerplan.repository;

import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.ucdm.leisure.dinnerplan.model.User;
import java.util.List;

@Repository
public interface DinnerEventRepository extends JpaRepository<DinnerEvent, Long> {
    List<DinnerEvent> findAllByOrderByDeadlineDesc();

    List<DinnerEvent> findDistinctByOrganizerOrParticipantsContainsOrderByDeadlineDesc(User organizer,
            User participant);
}
