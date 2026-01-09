package it.ucdm.leisure.dinnerplan.persistence.sql;

import it.ucdm.leisure.dinnerplan.persistence.sql.entity.DinnerEventMessageSqlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaDinnerEventMessageRepository extends JpaRepository<DinnerEventMessageSqlEntity, Long> {

    List<DinnerEventMessageSqlEntity> findByEventIdOrderByTimestampAsc(Long eventId);
}
