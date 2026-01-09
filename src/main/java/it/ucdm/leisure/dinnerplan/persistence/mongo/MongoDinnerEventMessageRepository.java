package it.ucdm.leisure.dinnerplan.persistence.mongo;

import it.ucdm.leisure.dinnerplan.persistence.mongo.document.DinnerEventMessageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MongoDinnerEventMessageRepository extends MongoRepository<DinnerEventMessageDocument, Long> {
    List<DinnerEventMessageDocument> findByEventIdOrderByTimestampAsc(Long eventId);
}
