package it.ucdm.leisure.dinnerplan.persistence.mongo;

import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.persistence.DinnerEventRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.mongo.document.DinnerEventDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Profile("mongo")
public class MongoDinnerEventAdapter implements DinnerEventRepositoryPort {

    private final MongoDinnerEventRepository mongoDinnerEventRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public MongoDinnerEventAdapter(MongoDinnerEventRepository mongoDinnerEventRepository,
            SequenceGeneratorService sequenceGeneratorService) {
        this.mongoDinnerEventRepository = mongoDinnerEventRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public List<DinnerEvent> findDistinctByOrganizerOrParticipantsContainsOrderByDeadlineDesc(User organizer,
            User participant) {
        return mongoDinnerEventRepository.findByOrganizer_IdOrParticipantIdsContainsOrderByDeadlineDesc(
                organizer != null ? organizer.getId() : null,
                participant != null ? participant.getId() : null).stream().map(DinnerEventDocument::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<DinnerEvent> findByDeadlineBeforeAndStatus(LocalDateTime deadline, DinnerEvent.EventStatus status) {
        return mongoDinnerEventRepository.findByDeadlineBeforeAndStatus(deadline, status)
                .stream().map(DinnerEventDocument::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<DinnerEvent> findById(Long id) {
        return mongoDinnerEventRepository.findById(id).map(DinnerEventDocument::toDomain);
    }

    @Override
    public DinnerEvent save(DinnerEvent event) {
        DinnerEventDocument doc = DinnerEventDocument.fromDomain(event);
        if (doc.getId() == null) {
            doc.setId(sequenceGeneratorService.generateSequence(DinnerEventDocument.class.getSimpleName()));
        }
        return mongoDinnerEventRepository.save(doc).toDomain();
    }

    @Override
    public void deleteById(Long id) {
        mongoDinnerEventRepository.deleteById(id);
    }

    @Override
    public List<DinnerEvent> findAll() {
        return mongoDinnerEventRepository.findAll().stream()
                .map(DinnerEventDocument::toDomain)
                .collect(Collectors.toList());
    }
}
