package it.ucdm.leisure.dinnerplan.persistence.sql;

import it.ucdm.leisure.dinnerplan.persistence.sql.entity.ProposalDateSqlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaProposalDateRepository extends JpaRepository<ProposalDateSqlEntity, Long> {
}
