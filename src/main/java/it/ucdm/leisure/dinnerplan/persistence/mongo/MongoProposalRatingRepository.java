package it.ucdm.leisure.dinnerplan.persistence.mongo;

import it.ucdm.leisure.dinnerplan.persistence.mongo.document.ProposalRatingDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MongoProposalRatingRepository extends MongoRepository<ProposalRatingDocument, Long> {
    List<ProposalRatingDocument> findByProposalId(Long proposalId);

    Optional<ProposalRatingDocument> findByProposalIdAndUser_Id(Long proposalId, Long userId);
}
