package it.ucdm.leisure.dinnerplan.repository;

import it.ucdm.leisure.dinnerplan.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.model.Proposal;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findByProposal_DinnerEvent_IdAndUser_Id(Long eventId, Long userId);

    Optional<Vote> findByUserAndProposal(User user, Proposal proposal);
}
