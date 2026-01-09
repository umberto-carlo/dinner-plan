package it.ucdm.leisure.dinnerplan.persistence.mongo;

import it.ucdm.leisure.dinnerplan.model.Proposal;
import it.ucdm.leisure.dinnerplan.model.ProposalRating;
import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.persistence.ProposalRatingRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.mongo.document.ProposalRatingDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Profile("mongo")
public class MongoProposalRatingAdapter implements ProposalRatingRepositoryPort {

    private final MongoProposalRatingRepository mongoProposalRatingRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public MongoProposalRatingAdapter(MongoProposalRatingRepository mongoProposalRatingRepository,
            SequenceGeneratorService sequenceGeneratorService) {
        this.mongoProposalRatingRepository = mongoProposalRatingRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public Optional<ProposalRating> findByUserAndProposal(User user, Proposal proposal) {
        if (user == null || proposal == null)
            return Optional.empty();
        return mongoProposalRatingRepository.findByProposalIdAndUser_Id(proposal.getId(), user.getId())
                .map(this::toDomainWithExtras);
    }

    @Override
    public long countByProposalAndIsLikedTrue(Proposal proposal) {
        if (proposal == null)
            return 0;
        return mongoProposalRatingRepository.findByProposalId(proposal.getId()).stream()
                .filter(ProposalRatingDocument::isLiked)
                .count();
    }

    @Override
    public long countByProposalAndIsLikedFalse(Proposal proposal) {
        if (proposal == null)
            return 0;
        return mongoProposalRatingRepository.findByProposalId(proposal.getId()).stream()
                .filter(doc -> !doc.isLiked())
                .count();
    }

    @Override
    public ProposalRating save(ProposalRating rating) {
        Long proposalId = (rating.getProposal() != null) ? rating.getProposal().getId() : null;
        ProposalRatingDocument doc = ProposalRatingDocument.fromDomain(rating, proposalId);
        if (doc.getId() == null) {
            doc.setId(sequenceGeneratorService.generateSequence(ProposalRatingDocument.class.getSimpleName()));
        }
        return mongoProposalRatingRepository.save(doc).toDomain();
    }

    @Override
    public void delete(ProposalRating rating) {
        if (rating != null && rating.getId() != null) {
            mongoProposalRatingRepository.deleteById(rating.getId());
        }
    }

    private ProposalRating toDomainWithExtras(ProposalRatingDocument doc) {
        ProposalRating rating = doc.toDomain();
        if (doc.getProposalId() != null) {
            Proposal p = new Proposal();
            p.setId(doc.getProposalId());
            rating.setProposal(p);
        }
        return rating;
    }
}
