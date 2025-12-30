package it.ucdm.leisure.dinnerplan.repository;

import it.ucdm.leisure.dinnerplan.model.Proposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    List<Proposal> findTop50ByOrderByDinnerEvent_DeadlineDesc();

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "votes", "ratings" })
    List<Proposal> findAllByDinnerEventId(Long eventId);
}
