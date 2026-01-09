package it.ucdm.leisure.dinnerplan.persistence.sql;

import it.ucdm.leisure.dinnerplan.persistence.sql.entity.ProposalRatingSqlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaProposalRatingRepository extends JpaRepository<ProposalRatingSqlEntity, Long> {
    Optional<ProposalRatingSqlEntity> findByUser_IdAndProposal_Id(Long userId, Long proposalId);

    long countByProposal_IdAndIsLikedTrue(Long proposalId);

    long countByProposal_IdAndIsLikedFalse(Long proposalId);
}
