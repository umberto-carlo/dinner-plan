package it.ucdm.leisure.dinnerplan.persistence.sql;

import it.ucdm.leisure.dinnerplan.model.DinnerEventMessage;
import it.ucdm.leisure.dinnerplan.persistence.DinnerEventMessageRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.sql.entity.DinnerEventMessageSqlEntity;
import it.ucdm.leisure.dinnerplan.persistence.sql.entity.DinnerEventSqlEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("sql")
public class SqlDinnerEventMessageAdapter implements DinnerEventMessageRepositoryPort {

    private final JpaDinnerEventMessageRepository jpaDinnerEventMessageRepository;
    private final JpaDinnerEventRepository jpaDinnerEventRepository;

    public SqlDinnerEventMessageAdapter(JpaDinnerEventMessageRepository jpaDinnerEventMessageRepository,
            JpaDinnerEventRepository jpaDinnerEventRepository) {
        this.jpaDinnerEventMessageRepository = jpaDinnerEventMessageRepository;
        this.jpaDinnerEventRepository = jpaDinnerEventRepository;
    }

    @Override
    public DinnerEventMessage save(DinnerEventMessage message) {
        if (message.getEvent() == null || message.getEvent().getId() == null) {
            throw new IllegalArgumentException("Message must belong to an event");
        }
        DinnerEventSqlEntity eventEntity = jpaDinnerEventRepository.getReferenceById(message.getEvent().getId());
        DinnerEventMessageSqlEntity entity = DinnerEventMessageSqlEntity.fromDomain(message, eventEntity);
        return jpaDinnerEventMessageRepository.save(entity).toDomain();
    }

    @Override
    public List<DinnerEventMessage> findByEventIdOrderByTimestampAsc(Long eventId) {
        return jpaDinnerEventMessageRepository.findByEventIdOrderByTimestampAsc(eventId)
                .stream()
                .map(DinnerEventMessageSqlEntity::toDomain)
                .collect(Collectors.toList());
    }
}
