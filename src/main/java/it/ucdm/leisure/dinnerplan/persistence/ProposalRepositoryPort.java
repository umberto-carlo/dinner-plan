package it.ucdm.leisure.dinnerplan.persistence;

import it.ucdm.leisure.dinnerplan.model.Proposal;
import java.util.List;
import java.util.Optional;

public interface ProposalRepositoryPort {
    Proposal save(Proposal proposal);

    Optional<Proposal> findById(Long id);

    List<Proposal> findAll();

    List<Proposal> findAllByDinnerEventId(Long eventId);

    Optional<Proposal> findByLocationIgnoreCaseAndAddressIgnoreCase(String location, String address);

    void deleteById(Long id);
}
