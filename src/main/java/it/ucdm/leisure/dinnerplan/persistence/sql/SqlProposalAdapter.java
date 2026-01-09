package it.ucdm.leisure.dinnerplan.persistence.sql;

import it.ucdm.leisure.dinnerplan.model.Proposal;
import it.ucdm.leisure.dinnerplan.persistence.ProposalRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.sql.entity.ProposalSqlEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Profile("sql")
public class SqlProposalAdapter implements ProposalRepositoryPort {

    private final JpaProposalRepository jpaProposalRepository;

    public SqlProposalAdapter(JpaProposalRepository jpaProposalRepository) {
        this.jpaProposalRepository = jpaProposalRepository;
    }

    @Override
    public List<Proposal> findAllByDinnerEventId(Long eventId) {
        return jpaProposalRepository.findAllByDinnerEventId(eventId).stream()
                .map(ProposalSqlEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Proposal> findByLocationIgnoreCaseAndAddressIgnoreCase(String location, String address) {
        return jpaProposalRepository.findByLocationIgnoreCaseAndAddressIgnoreCase(location, address)
                .map(ProposalSqlEntity::toDomain);
    }

    @Override
    public Proposal save(Proposal proposal) {
        ProposalSqlEntity entity = ProposalSqlEntity.fromDomain(proposal);
        ProposalSqlEntity savedEntity = jpaProposalRepository.save(entity);
        return savedEntity.toDomain();
    }

    @Override
    public Optional<Proposal> findById(Long id) {
        return jpaProposalRepository.findById(id)
                .map(ProposalSqlEntity::toDomain);
    }

    @Override
    public List<Proposal> findAll() {
        return jpaProposalRepository.findAll().stream()
                .map(ProposalSqlEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaProposalRepository.deleteById(id);
    }
}
