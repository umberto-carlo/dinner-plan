package it.ucdm.leisure.dinnerplan.persistence.mongo;

import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import it.ucdm.leisure.dinnerplan.model.Proposal;
import it.ucdm.leisure.dinnerplan.persistence.ProposalRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.mongo.document.ProposalDocument;
import it.ucdm.leisure.dinnerplan.persistence.mongo.document.ProposalRatingDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Profile("mongo")
public class MongoProposalAdapter implements ProposalRepositoryPort {

    private final MongoProposalRepository mongoProposalRepository;
    private final MongoProposalRatingRepository mongoProposalRatingRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public MongoProposalAdapter(MongoProposalRepository mongoProposalRepository,
            MongoProposalRatingRepository mongoProposalRatingRepository,
            SequenceGeneratorService sequenceGeneratorService) {
        this.mongoProposalRepository = mongoProposalRepository;
        this.mongoProposalRatingRepository = mongoProposalRatingRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public List<Proposal> findAllByDinnerEventId(Long dinnerEventId) {
        return mongoProposalRepository.findAllByDinnerEventId(dinnerEventId).stream()
                .map(this::toDomainWithExtras)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Proposal> findById(Long id) {
        return mongoProposalRepository.findById(id).map(this::toDomainWithExtras);
    }

    @Override
    public Proposal save(Proposal proposal) {
        ProposalDocument doc = ProposalDocument.fromDomain(proposal);
        if (doc.getId() == null) {
            doc.setId(sequenceGeneratorService.generateSequence(ProposalDocument.class.getSimpleName()));
        }
        if (doc.getDates() != null) {
            doc.getDates().forEach(date -> {
                if (date.getId() == null) {
                    date.setId(sequenceGeneratorService.generateSequence("ProposalDate"));
                }
            });
        }
        return toDomainWithExtras(mongoProposalRepository.save(doc));
    }

    @Override
    public void deleteById(Long id) {
        mongoProposalRepository.deleteById(id);
    }

    @Override
    public List<Proposal> findAll() {
        return mongoProposalRepository.findAll().stream()
                .map(this::toDomainWithExtras)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Proposal> findByLocationIgnoreCaseAndAddressIgnoreCase(String location, String address) {
        return mongoProposalRepository.findByLocationIgnoreCaseAndAddressIgnoreCase(location, address)
                .map(this::toDomainWithExtras);
    }

    private Proposal toDomainWithExtras(ProposalDocument doc) {
        Proposal proposal = doc.toDomain();
        proposal.setRatings(mongoProposalRatingRepository.findByProposalId(doc.getId()).stream()
                .map(ProposalRatingDocument::toDomain)
                .collect(Collectors.toSet()));
        if (doc.getDinnerEventId() != null) {
            DinnerEvent event = new DinnerEvent();
            event.setId(doc.getDinnerEventId());
            proposal.setDinnerEvent(event);
        }
        return proposal;
    }
}
