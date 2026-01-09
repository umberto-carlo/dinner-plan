package it.ucdm.leisure.dinnerplan.persistence.mongo;

import it.ucdm.leisure.dinnerplan.persistence.mongo.document.VoteDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MongoVoteRepository extends MongoRepository<VoteDocument, Long> {
    List<VoteDocument> findByProposalDateId(Long proposalDateId);

    Optional<VoteDocument> findByProposalDateIdAndUser_Id(Long proposalDateId, Long userId);

    List<VoteDocument> findByEventIdAndUser_Id(Long eventId, Long userId);
}
