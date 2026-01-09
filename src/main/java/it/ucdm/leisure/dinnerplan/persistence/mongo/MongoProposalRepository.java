package it.ucdm.leisure.dinnerplan.persistence.mongo;

import it.ucdm.leisure.dinnerplan.persistence.mongo.document.ProposalDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface MongoProposalRepository extends MongoRepository<ProposalDocument, Long> {
    List<ProposalDocument> findAllByDinnerEventId(Long dinnerEventId);

    Optional<ProposalDocument> findByLocationIgnoreCaseAndAddressIgnoreCase(String location, String address);
}
