package it.ucdm.leisure.dinnerplan.persistence.sql;

import it.ucdm.leisure.dinnerplan.persistence.sql.entity.ProposalSqlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaProposalRepository extends JpaRepository<ProposalSqlEntity, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "dates" })
    List<ProposalSqlEntity> findAllByDinnerEventId(Long eventId);

    Optional<ProposalSqlEntity> findByLocationIgnoreCaseAndAddressIgnoreCase(String location, String address);
}
