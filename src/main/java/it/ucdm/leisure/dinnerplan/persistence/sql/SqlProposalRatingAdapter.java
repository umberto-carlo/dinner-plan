package it.ucdm.leisure.dinnerplan.persistence.sql;

import it.ucdm.leisure.dinnerplan.model.Proposal;
import it.ucdm.leisure.dinnerplan.model.ProposalRating;
import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.persistence.ProposalRatingRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.sql.entity.ProposalRatingSqlEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Profile("sql")
public class SqlProposalRatingAdapter implements ProposalRatingRepositoryPort {

    private final JpaProposalRatingRepository jpaProposalRatingRepository;

    public SqlProposalRatingAdapter(JpaProposalRatingRepository jpaProposalRatingRepository) {
        this.jpaProposalRatingRepository = jpaProposalRatingRepository;
    }

    @Override
    public Optional<ProposalRating> findByUserAndProposal(User user, Proposal proposal) {
        if (user == null || proposal == null) {
            return Optional.empty();
        }
        return jpaProposalRatingRepository.findByUser_IdAndProposal_Id(user.getId(), proposal.getId())
                .map(ProposalRatingSqlEntity::toDomain);
    }

    @Override
    public long countByProposalAndIsLikedTrue(Proposal proposal) {
        if (proposal == null)
            return 0;
        return jpaProposalRatingRepository.countByProposal_IdAndIsLikedTrue(proposal.getId());
    }

    @Override
    public long countByProposalAndIsLikedFalse(Proposal proposal) {
        if (proposal == null)
            return 0;
        return jpaProposalRatingRepository.countByProposal_IdAndIsLikedFalse(proposal.getId());
    }

    @Override
    public ProposalRating save(ProposalRating rating) {
        ProposalRatingSqlEntity entity = ProposalRatingSqlEntity.fromDomain(rating);
        return jpaProposalRatingRepository.save(entity).toDomain();
    }

    @Override
    public void delete(ProposalRating rating) {
        if (rating != null && rating.getId() != null) {
            jpaProposalRatingRepository.deleteById(rating.getId());
        }
    }
}
