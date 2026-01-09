package it.ucdm.leisure.dinnerplan.persistence;

import it.ucdm.leisure.dinnerplan.model.ProposalDate;
import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.model.Vote;
import java.util.List;
import java.util.Optional;

public interface VoteRepositoryPort {
    List<Vote> findByProposalDate_DinnerEvent_IdAndUser_Id(Long eventId, Long userId);

    Optional<Vote> findByUserAndProposalDate(User user, ProposalDate proposalDate);

    Vote save(Vote vote);

    void delete(Vote vote);
}
