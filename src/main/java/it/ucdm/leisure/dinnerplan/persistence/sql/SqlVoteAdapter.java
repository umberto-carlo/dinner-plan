package it.ucdm.leisure.dinnerplan.persistence.sql;

import it.ucdm.leisure.dinnerplan.model.ProposalDate;
import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.model.Vote;
import it.ucdm.leisure.dinnerplan.persistence.VoteRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.sql.entity.VoteSqlEntity;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Profile("sql")
public class SqlVoteAdapter implements VoteRepositoryPort {

    private final JpaVoteRepository jpaVoteRepository;

    public SqlVoteAdapter(JpaVoteRepository jpaVoteRepository) {
        this.jpaVoteRepository = jpaVoteRepository;
    }

    @Override
    public List<Vote> findByProposalDate_DinnerEvent_IdAndUser_Id(Long eventId, Long userId) {
        return jpaVoteRepository.findByProposalDate_Proposal_DinnerEvent_IdAndUser_Id(eventId, userId)
                .stream()
                .map(VoteSqlEntity::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Vote> findByUserAndProposalDate(User user, ProposalDate proposalDate) {
        if (user == null || proposalDate == null) {
            return Optional.empty();
        }
        return jpaVoteRepository.findByUser_IdAndProposalDate_Id(user.getId(), proposalDate.getId())
                .map(VoteSqlEntity::toDomain);
    }

    @Override
    public Vote save(Vote vote) {
        VoteSqlEntity entity = VoteSqlEntity.fromDomain(vote);
        return jpaVoteRepository.save(entity).toDomain();
    }

    @Override
    public void delete(Vote vote) {
        if (vote != null && vote.getId() != null) {
            jpaVoteRepository.deleteById(vote.getId());
        }
    }
}
