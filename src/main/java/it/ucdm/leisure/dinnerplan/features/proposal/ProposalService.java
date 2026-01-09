package it.ucdm.leisure.dinnerplan.features.proposal;

import it.ucdm.leisure.dinnerplan.features.proposal.dto.ProposalSuggestionDTO;
import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import it.ucdm.leisure.dinnerplan.model.Proposal;
import it.ucdm.leisure.dinnerplan.model.ProposalDate;
import it.ucdm.leisure.dinnerplan.persistence.DinnerEventRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.ProposalRepositoryPort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ProposalService {

    private final ProposalRepositoryPort proposalRepository;
    private final DinnerEventRepositoryPort dinnerEventRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();

    public ProposalService(ProposalRepositoryPort proposalRepository, DinnerEventRepositoryPort dinnerEventRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.proposalRepository = proposalRepository;
        this.dinnerEventRepository = dinnerEventRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public List<Proposal> getProposalsForEvent(Long eventId) {
        return proposalRepository.findAllByDinnerEventId(eventId);
    }

    @Transactional
    public void addProposal(Long eventId, List<LocalDateTime> dateOptions, String location, String address,
            String description) {
        Objects.requireNonNull(eventId, "Event ID must not be null");
        DinnerEvent event = dinnerEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id:" + eventId));

        if (event.getStatus() == DinnerEvent.EventStatus.DECIDED) {
            throw new IllegalStateException("Event is already decided");
        }

        // Check if proposal already exists FOR THIS EVENT
        Proposal proposal = event.getProposals().stream()
                .filter(p -> p.getLocation().equalsIgnoreCase(location) &&
                        (address == null || p.getAddress().equalsIgnoreCase(address)))
                .findFirst()
                .orElse(null);

        if (proposal == null) {
            proposal = new Proposal();
            proposal.setDinnerEvent(event);
            proposal.setLocation(location);
            proposal.setAddress(address);
            proposal.setDescription(description);
            proposal.setDates(new ArrayList<>());

            event.getProposals().add(proposal);
            // Proposal will be saved via cascade or we can save it explicitly if needed
        } else {
            if ((proposal.getDescription() == null || proposal.getDescription().isBlank()) && description != null
                    && !description.isBlank()) {
                proposal.setDescription(description);
            }
        }

        if (dateOptions != null) {
            for (LocalDateTime d : dateOptions) {
                if (d.isBefore(LocalDateTime.now())) {
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                            .ofPattern("dd/MM/yyyy HH:mm");
                    throw new IllegalArgumentException(
                            "Le date delle proposte devono essere future (inserito: " + d.format(formatter) + ")");
                }
                if (d.isBefore(event.getDeadline())) {
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                            .ofPattern("dd/MM/yyyy HH:mm");
                    throw new IllegalArgumentException(
                            "Impossibile aggiungere una data precedente alla scadenza dell'evento ("
                                    + event.getDeadline().format(formatter) + ")");
                }
                Proposal finalProposal = proposal;
                boolean exists = proposal.getDates().stream()
                        .anyMatch(existing -> existing.getDate().isEqual(d));
                if (!exists) {
                    ProposalDate newDate = new ProposalDate();
                    newDate.setDate(d);
                    newDate.setProposal(finalProposal);
                    // newDate.setDinnerEvent(event); // Not needed as derived? But if mapped:
                    // ProposalDate doesn't have dinnerEvent field?
                    // Wait, ProposalDateSqlEntity had it. ProposalDate Domain?
                    // Previous step 1517: ProposalDate Domain does NOT have dinnerEvent field.
                    // So we don't set it.

                    proposal.getDates().add(newDate);
                }
            }
        }

        // Ensure changes to proposal are saved?
        // If proposal is new, we need to save it?
        // `event.getProposals().add(proposal)`
        // `dinnerEventRepository.save(event)` should cascade to proposals.
        // But `SqlDinnerEventAdapter` implementation:
        // `DinnerEventSqlEntity.fromDomain(event)`
        // -> `proposals.stream().map(ProposalSqlEntity::fromDomain)`
        // `fromDomain` creates new `ProposalSqlEntity` with ID (if present) or null.
        // JPA `save` will cascade persist/merge.
        // So saving event should be enough IF cascading is set on
        // `DinnerEventSqlEntity.proposals`.
        // `DinnerEventSqlEntity` has `@ManyToMany(cascade = { CascadeType.PERSIST,
        // CascadeType.MERGE })`.
        // YES.

        dinnerEventRepository.save(event);

        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        messagingTemplate.convertAndSend("/topic/events/" + eventId, "update");
                    }
                });
    }

    @Transactional
    public void addProposalFromSuggestion(Long eventId, List<LocalDateTime> dateOptions, String location,
            String address,
            String description, String username) {
        Objects.requireNonNull(eventId, "Event ID must not be null");
        DinnerEvent event = dinnerEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id"));

        if (!event.getOrganizer().getUsername().equals(username)) {
            throw new SecurityException("Only organizer can add proposals");
        }

        if (event.getStatus() == DinnerEvent.EventStatus.DECIDED) {
            throw new IllegalStateException("Event is already decided");
        }

        addProposal(eventId, dateOptions, location, address, description);
    }

    @Transactional
    public int addBatchProposalsFromSuggestion(Long eventId, List<LocalDateTime> dateOptions,
            List<String> encodedProposals, String username) {
        Objects.requireNonNull(eventId, "Event ID must not be null");
        DinnerEvent event = dinnerEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id"));

        if (!event.getOrganizer().getUsername().equals(username)) {
            throw new SecurityException("Only organizer can add proposals");
        }
        if (event.getStatus() == DinnerEvent.EventStatus.DECIDED) {
            throw new IllegalStateException("Event is already decided");
        }
        if (dateOptions == null || encodedProposals == null || dateOptions.size() != encodedProposals.size()) {
            throw new IllegalArgumentException("Mismatch between proposals and dates");
        }

        int addedCount = 0;
        for (int i = 0; i < encodedProposals.size(); i++) {
            String encoded = encodedProposals.get(i);
            LocalDateTime date = dateOptions.get(i);
            try {
                String json = new String(java.util.Base64.getDecoder().decode(encoded));
                ProposalSuggestionDTO dto = mapper.readValue(json, ProposalSuggestionDTO.class);

                addProposal(eventId, List.of(date), dto.getLocation(), dto.getAddress(), dto.getDescription());
                addedCount++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return addedCount;

    }

    @Transactional
    public void deleteProposalDate(Long proposalId, Long dateId, String username) {
        Proposal proposal = proposalRepository.findById(Objects.requireNonNull(proposalId))
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));

        if (proposal.getDates().size() <= 1) {
            throw new IllegalStateException(
                    "Cannot remove the last date. Only proposals with multiple dates can have dates removed.");
        }

        ProposalDate dateToRemove = proposal.getDates().stream()
                .filter(d -> d.getId().equals(dateId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Date not found in proposal"));

        proposal.getDates().remove(dateToRemove);
        proposalRepository.save(proposal);
    }
}
