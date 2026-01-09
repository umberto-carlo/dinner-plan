package it.ucdm.leisure.dinnerplan.persistence.mongo;

import it.ucdm.leisure.dinnerplan.model.ProposalDate;
import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.model.Vote;
import it.ucdm.leisure.dinnerplan.persistence.VoteRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.mongo.document.VoteDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Profile("mongo")
public class MongoVoteAdapter implements VoteRepositoryPort {

    private final MongoVoteRepository mongoVoteRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public MongoVoteAdapter(MongoVoteRepository mongoVoteRepository,
            SequenceGeneratorService sequenceGeneratorService) {
        this.mongoVoteRepository = mongoVoteRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public List<Vote> findByProposalDate_DinnerEvent_IdAndUser_Id(Long eventId, Long userId) {
        return mongoVoteRepository.findByEventIdAndUser_Id(eventId, userId).stream()
                .map(this::toDomainWithExtras)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Vote> findByUserAndProposalDate(User user, ProposalDate proposalDate) {
        if (user == null || proposalDate == null)
            return Optional.empty();
        return mongoVoteRepository.findByProposalDateIdAndUser_Id(proposalDate.getId(), user.getId())
                .map(this::toDomainWithExtras);
    }

    @Override
    public Vote save(Vote vote) {
        Long eventId = null;
        Long proposalDateId = null;

        if (vote.getProposalDate() != null) {
            proposalDateId = vote.getProposalDate().getId();
            if (vote.getProposalDate().getProposal() != null &&
                    vote.getProposalDate().getProposal().getDinnerEvent() != null) {
                eventId = vote.getProposalDate().getProposal().getDinnerEvent().getId();
            }
        }

        VoteDocument doc = VoteDocument.fromDomain(vote, eventId, proposalDateId);
        if (doc.getId() == null) {
            doc.setId(sequenceGeneratorService.generateSequence(VoteDocument.class.getSimpleName()));
        }
        return mongoVoteRepository.save(doc).toDomain();
    }

    @Override
    public void delete(Vote vote) {
        if (vote != null && vote.getId() != null) {
            mongoVoteRepository.deleteById(vote.getId());
        }
    }

    private Vote toDomainWithExtras(VoteDocument doc) {
        Vote vote = doc.toDomain();
        if (doc.getProposalDateId() != null) {
            ProposalDate pd = new ProposalDate();
            pd.setId(doc.getProposalDateId());
            vote.setProposalDate(pd);
        }
        return vote;
    }
}
