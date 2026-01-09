package it.ucdm.leisure.dinnerplan.features.proposal;

import it.ucdm.leisure.dinnerplan.features.proposal.dto.ProposalSuggestionDTO;
import it.ucdm.leisure.dinnerplan.features.event.DinnerEvent;

import it.ucdm.leisure.dinnerplan.features.event.DinnerEventRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final DinnerEventRepository dinnerEventRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();

    public ProposalService(ProposalRepository proposalRepository, DinnerEventRepository dinnerEventRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.proposalRepository = proposalRepository;
        this.dinnerEventRepository = dinnerEventRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public List<Proposal> getProposalsForEvent(Long eventId) {
        return proposalRepository.findAllByDinnerEventsId(eventId);
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

        // Check if proposal already exists in DB (Global Uniqueness)
        Proposal proposal = proposalRepository.findByLocationIgnoreCaseAndAddressIgnoreCase(location, address)
                .orElse(null);

        if (proposal == null) {
            proposal = Proposal.builder()
                    .dinnerEvents(new ArrayList<>(List.of(event)))
                    .location(location)
                    .address(address)
                    .description(description)
                    .dates(new ArrayList<>())
                    .build();
            event.getProposals().add(proposal);
        } else {
            // Found existing global proposal. Check if it's already linked to this event.
            // If the proposal is not associated with the event, we associate them.
            if (!proposal.getDinnerEvents().contains(event)) {
                proposal.getDinnerEvents().add(event);
                event.getProposals().add(proposal);
            }
            // Also, update description if the new one is provided and significant?
            // Requirement says "transparently use existing". Usually implies keeping
            // existing data or merging.
            // We'll keep existing description to avoid overriding with potentially less
            // info, or update if empty.
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
                boolean exists = proposal.getDates().stream()
                        .anyMatch(existing -> existing.getDate().isEqual(d) && existing.getDinnerEvent().equals(event));
                if (!exists) {
                    proposal.getDates()
                            .add(ProposalDate.builder().date(d).proposal(proposal).dinnerEvent(event).build());
                }
            }
        }

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
        // Reuse validation logic effectively via helper or direct call
        // For simplicity, re-fetch validation context
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
