package it.ucdm.leisure.dinnerplan.features.proposal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "dates" })
    List<Proposal> findAllByDinnerEventsId(Long eventId);

    java.util.Optional<Proposal> findByLocationIgnoreCaseAndAddressIgnoreCase(String location, String address);

    List<Proposal> findByDinnerEvents_IdNot(Long eventId);
}
