package it.ucdm.leisure.dinnerplan.features.proposal;

import it.ucdm.leisure.dinnerplan.features.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProposalRatingRepository extends JpaRepository<ProposalRating, Long> {
    Optional<ProposalRating> findByUserAndProposal(User user, Proposal proposal);

    long countByProposalAndIsLikedTrue(Proposal proposal);

    long countByProposalAndIsLikedFalse(Proposal proposal);
}
