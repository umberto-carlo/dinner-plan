package it.ucdm.leisure.dinnerplan.features.proposal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import it.ucdm.leisure.dinnerplan.features.user.User;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findByProposalDate_DinnerEvent_IdAndUser_Id(Long eventId, Long userId);

    Optional<Vote> findByUserAndProposalDate(User user, ProposalDate proposalDate);
}
