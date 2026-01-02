package it.ucdm.leisure.dinnerplan.service;

import it.ucdm.leisure.dinnerplan.dto.ProposalSuggestionDTO;
import it.ucdm.leisure.dinnerplan.model.*;
import it.ucdm.leisure.dinnerplan.repository.ProposalRepository;
import it.ucdm.leisure.dinnerplan.repository.DinnerEventRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final DinnerEventRepository dinnerEventRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

    public ProposalService(ProposalRepository proposalRepository, DinnerEventRepository dinnerEventRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.proposalRepository = proposalRepository;
        this.dinnerEventRepository = dinnerEventRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public List<Proposal> getProposalsForEvent(Long eventId) {
        return proposalRepository.findAllByDinnerEventId(eventId);
    }

    @Transactional
    public void addProposal(Long eventId, LocalDateTime dateOption, String location, String address,
            String description) {
        DinnerEvent event = dinnerEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id:" + eventId));

        if (event.getStatus() == DinnerEvent.EventStatus.DECIDED) {
            throw new IllegalStateException("Event is already decided");
        }

        Proposal proposal = Proposal.builder()
                .dinnerEvent(event)
                .dateOption(dateOption)
                .location(location)
                .address(address)
                .description(description)
                .build();

        event.getProposals().add(proposal);
        dinnerEventRepository.save(event);

        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        messagingTemplate.convertAndSend("/topic/events/" + eventId, "update");
                    }
                });
    }

    @Transactional
    public void addProposalFromSuggestion(Long eventId, LocalDateTime dateOption, String location, String address,
            String description, String username) {
        DinnerEvent event = dinnerEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id"));

        if (!event.getOrganizer().getUsername().equals(username)) {
            throw new SecurityException("Only organizer can add proposals");
        }

        if (event.getStatus() == DinnerEvent.EventStatus.DECIDED) {
            throw new IllegalStateException("Event is already decided");
        }

        boolean exists = event.getProposals().stream()
                .anyMatch(p -> p.getLocation().equalsIgnoreCase(location));

        if (exists) {
            throw new IllegalArgumentException("Questa proposta è già presente nell'evento specificato.");
        }

        addProposal(eventId, dateOption, location, address, description);
    }

    @Transactional
    public int addBatchProposalsFromSuggestion(Long eventId, List<LocalDateTime> dateOptions,
            List<String> encodedProposals, String username) {
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

                boolean exists = event.getProposals().stream()
                        .anyMatch(p -> p.getLocation().equalsIgnoreCase(dto.getLocation()));

                if (!exists) {
                    addProposal(eventId, date, dto.getLocation(), dto.getAddress(), dto.getDescription());
                    addedCount++;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return addedCount;
    }

    @Transactional
    public void addGlobalProposal(String location, String address, String description) {
        Proposal proposal = Proposal.builder()
                .dinnerEvent(null)
                .dateOption(null)
                .location(location)
                .address(address)
                .description(description)
                .build();

        proposalRepository.save(proposal);

        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        messagingTemplate.convertAndSend("/topic/events", "update");
                    }
                });
    }

    public List<ProposalSuggestionDTO> getProposalSuggestions() {
        List<Proposal> allProposals = proposalRepository.findAll();
        List<ProposalSuggestionDTO> suggestions = new ArrayList<>();
        java.util.Map<String, ProposalSuggestionDTO> map = new java.util.HashMap<>();

        for (Proposal p : allProposals) {
            String key = (p.getLocation() + "|" + (p.getAddress() != null ? p.getAddress() : "")).toLowerCase();
            ProposalSuggestionDTO dto = map.getOrDefault(key, ProposalSuggestionDTO.builder()
                    .location(p.getLocation())
                    .address(p.getAddress())
                    .description(p.getDescription())
                    .totalLikes(0)
                    .totalDislikes(0)
                    .usageCount(0)
                    .build());

            dto.setUsageCount(dto.getUsageCount() + 1);

            if (p.getRatings() != null) {
                for (ProposalRating r : p.getRatings()) {
                    if (r.isLiked())
                        dto.setTotalLikes(dto.getTotalLikes() + 1);
                    else
                        dto.setTotalDislikes(dto.getTotalDislikes() + 1);
                }
            }

            if (p.getDescription() != null && !p.getDescription().isBlank()) {
                dto.setDescription(p.getDescription());
            }
            map.put(key, dto);
        }

        for (ProposalSuggestionDTO dto : map.values()) {
            try {
                String json = mapper.writeValueAsString(dto);
                String encoded = java.util.Base64.getEncoder().encodeToString(json.getBytes());
                dto.setEncodedData(encoded);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        suggestions.addAll(map.values());
        suggestions.sort((a, b) -> {
            int likeCompare = Long.compare(b.getTotalLikes(), a.getTotalLikes());
            if (likeCompare != 0)
                return likeCompare;
            return Integer.compare(b.getUsageCount(), a.getUsageCount());
        });
        return suggestions;
    }
}
