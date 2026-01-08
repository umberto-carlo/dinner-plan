package it.ucdm.leisure.dinnerplan.features.proposal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProposalDateRepository extends JpaRepository<ProposalDate, Long> {
}
