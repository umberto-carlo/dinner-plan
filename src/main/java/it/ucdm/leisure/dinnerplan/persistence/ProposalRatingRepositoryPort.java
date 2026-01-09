package it.ucdm.leisure.dinnerplan.persistence;

import it.ucdm.leisure.dinnerplan.model.Proposal;
import it.ucdm.leisure.dinnerplan.model.ProposalRating;
import it.ucdm.leisure.dinnerplan.model.User;
import java.util.Optional;

public interface ProposalRatingRepositoryPort {
    Optional<ProposalRating> findByUserAndProposal(User user, Proposal proposal);

    long countByProposalAndIsLikedTrue(Proposal proposal);

    long countByProposalAndIsLikedFalse(Proposal proposal);

    ProposalRating save(ProposalRating rating);

    void delete(ProposalRating rating);
}
