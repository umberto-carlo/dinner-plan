package it.ucdm.leisure.dinnerplan.persistence.mongo;

import it.ucdm.leisure.dinnerplan.persistence.mongo.document.DinnerEventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MongoDinnerEventRepository extends MongoRepository<DinnerEventDocument, Long> {
    List<DinnerEventDocument> findByOrganizer_IdOrParticipantIdsContainsOrderByDeadlineDesc(Long organizerId,
            Long participantId);

    List<DinnerEventDocument> findByDeadlineBeforeAndStatus(LocalDateTime deadline,
            it.ucdm.leisure.dinnerplan.model.DinnerEvent.EventStatus status);
}
