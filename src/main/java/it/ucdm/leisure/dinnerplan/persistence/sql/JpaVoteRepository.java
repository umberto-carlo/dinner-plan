package it.ucdm.leisure.dinnerplan.persistence.sql;

import it.ucdm.leisure.dinnerplan.persistence.sql.entity.VoteSqlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaVoteRepository extends JpaRepository<VoteSqlEntity, Long> {
    List<VoteSqlEntity> findByProposalDate_Proposal_DinnerEvent_IdAndUser_Id(Long eventId, Long userId);

    Optional<VoteSqlEntity> findByUser_IdAndProposalDate_Id(Long userId, Long proposalDateId);
}
