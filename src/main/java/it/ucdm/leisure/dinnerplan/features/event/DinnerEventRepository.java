package it.ucdm.leisure.dinnerplan.features.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import it.ucdm.leisure.dinnerplan.features.user.User;
import java.util.List;

@Repository
public interface DinnerEventRepository extends JpaRepository<DinnerEvent, Long> {
    List<DinnerEvent> findAllByOrderByDeadlineDesc();

    List<DinnerEvent> findDistinctByOrganizerOrParticipantsContainsOrderByDeadlineDesc(User organizer,
            User participant);
}
