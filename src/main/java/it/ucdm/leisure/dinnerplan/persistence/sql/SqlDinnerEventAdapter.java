package it.ucdm.leisure.dinnerplan.persistence.sql;

import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.persistence.DinnerEventRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.sql.entity.DinnerEventSqlEntity;
import it.ucdm.leisure.dinnerplan.persistence.sql.entity.UserSqlEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Profile("sql")
public class SqlDinnerEventAdapter implements DinnerEventRepositoryPort {

    private final JpaDinnerEventRepository jpaDinnerEventRepository;

    public SqlDinnerEventAdapter(JpaDinnerEventRepository jpaDinnerEventRepository) {
        this.jpaDinnerEventRepository = jpaDinnerEventRepository;
    }

    @Override
    public List<DinnerEvent> findDistinctByOrganizerOrParticipantsContainsOrderByDeadlineDesc(User organizer,
            User participant) {
        UserSqlEntity organizerEntity = UserSqlEntity.fromDomain(organizer);
        UserSqlEntity participantEntity = UserSqlEntity.fromDomain(participant);
        return jpaDinnerEventRepository
                .findDistinctByOrganizerOrParticipantsContainsOrderByDeadlineDesc(organizerEntity, participantEntity)
                .stream()
                .map(DinnerEventSqlEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<DinnerEvent> findByDeadlineBeforeAndStatus(LocalDateTime deadline, DinnerEvent.EventStatus status) {
        return jpaDinnerEventRepository.findByDeadlineBeforeAndStatus(deadline, status)
                .stream()
                .map(DinnerEventSqlEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<DinnerEvent> findById(Long id) {
        return jpaDinnerEventRepository.findById(id)
                .map(DinnerEventSqlEntity::toDomain);
    }

    @Override
    public DinnerEvent save(DinnerEvent event) {
        DinnerEventSqlEntity entity = DinnerEventSqlEntity.fromDomain(event);
        DinnerEventSqlEntity savedEntity = jpaDinnerEventRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public void deleteById(Long id) {
        jpaDinnerEventRepository.deleteById(id);
    }

    @Override
    public List<DinnerEvent> findAll() {
        return jpaDinnerEventRepository.findAll().stream()
                .map(DinnerEventSqlEntity::toDomain)
                .collect(Collectors.toList());
    }
}
