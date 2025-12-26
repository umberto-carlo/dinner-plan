package it.ucdm.leisure.dinnerplan.repository;

import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DinnerEventRepository extends JpaRepository<DinnerEvent, Long> {
    List<DinnerEvent> findAllByOrderByDeadlineDesc();
}
