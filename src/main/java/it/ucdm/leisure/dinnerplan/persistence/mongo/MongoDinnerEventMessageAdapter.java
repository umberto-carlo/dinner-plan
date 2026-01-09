package it.ucdm.leisure.dinnerplan.persistence.mongo;

import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import it.ucdm.leisure.dinnerplan.model.DinnerEventMessage;
import it.ucdm.leisure.dinnerplan.persistence.DinnerEventMessageRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.mongo.document.DinnerEventMessageDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@Profile("mongo")
public class MongoDinnerEventMessageAdapter implements DinnerEventMessageRepositoryPort {

    private final MongoDinnerEventMessageRepository mongoDinnerEventMessageRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public MongoDinnerEventMessageAdapter(MongoDinnerEventMessageRepository mongoDinnerEventMessageRepository,
            SequenceGeneratorService sequenceGeneratorService) {
        this.mongoDinnerEventMessageRepository = mongoDinnerEventMessageRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public DinnerEventMessage save(DinnerEventMessage message) {
        Long eventId = (message.getEvent() != null) ? message.getEvent().getId() : null;
        DinnerEventMessageDocument doc = DinnerEventMessageDocument.fromDomain(message, eventId);
        if (doc.getId() == null) {
            doc.setId(sequenceGeneratorService.generateSequence(DinnerEventMessageDocument.class.getSimpleName()));
        }
        return mongoDinnerEventMessageRepository.save(doc).toDomain();
    }

    @Override
    public List<DinnerEventMessage> findByEventIdOrderByTimestampAsc(Long eventId) {
        return mongoDinnerEventMessageRepository.findByEventIdOrderByTimestampAsc(eventId).stream()
                .map(doc -> {
                    DinnerEventMessage msg = doc.toDomain();
                    if (doc.getEventId() != null) {
                        DinnerEvent event = new DinnerEvent();
                        event.setId(doc.getEventId());
                        msg.setEvent(event);
                    }
                    return msg;
                })
                .collect(Collectors.toList());
    }
}
