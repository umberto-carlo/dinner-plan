package it.ucdm.leisure.dinnerplan.features.proposal;

import it.ucdm.leisure.dinnerplan.features.proposal.dto.ProposalSuggestionDTO;
import it.ucdm.leisure.dinnerplan.features.user.DietaryPreference;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class ProposalCatalogService {

    private final ProposalRepository proposalRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ProposalCatalogService(ProposalRepository proposalRepository, SimpMessagingTemplate messagingTemplate) {
        this.proposalRepository = proposalRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void addGlobalProposal(String location, String address, String description, String email, String phoneNumber, String website, Set<DietaryPreference> dietaryPreferences) {
        // Check if exists
        if (proposalRepository.findByLocationIgnoreCaseAndAddressIgnoreCase(location, address).isPresent()) {
            return; // Already exists, do nothing
        }

        Proposal proposal = Proposal.builder()
                .dinnerEvents(new ArrayList<>())
                .dates(new ArrayList<>())
                .location(location)
                .address(address)
                .description(description)
                .email(email)
                .phoneNumber(phoneNumber)
                .website(website)
                .dietaryPreferences(dietaryPreferences != null ? dietaryPreferences : new java.util.HashSet<>())
                .build();

        proposalRepository.save(Objects.requireNonNull(proposal));

        notifyUpdate();
    }
    
    @Transactional
    public void addGlobalProposal(String location, String address, String description, Set<DietaryPreference> dietaryPreferences) {
        addGlobalProposal(location, address, description, null, null, null, dietaryPreferences);
    }
    
    @Transactional
    public void addGlobalProposal(String location, String address, String description) {
        addGlobalProposal(location, address, description, null, null, null, null);
    }

    @Transactional
    public void updateGlobalProposalDietary(String location, String address, Set<DietaryPreference> dietaryPreferences) {
        Proposal proposal = proposalRepository.findByLocationIgnoreCaseAndAddressIgnoreCase(location, address)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        
        proposal.setDietaryPreferences(dietaryPreferences != null ? dietaryPreferences : new java.util.HashSet<>());
        proposalRepository.save(proposal);

        notifyUpdate();
    }

    @Transactional
    public void updateGlobalProposalDetails(String location, String address, String email, String phoneNumber, String website) {
        Proposal proposal = proposalRepository.findByLocationIgnoreCaseAndAddressIgnoreCase(location, address)
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        
        proposal.setEmail(email);
        proposal.setPhoneNumber(phoneNumber);
        proposal.setWebsite(website);
        proposalRepository.save(proposal);

        notifyUpdate();
    }

    private void notifyUpdate() {
        if (org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive()) {
            org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                    new org.springframework.transaction.support.TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            messagingTemplate.convertAndSend("/topic/events", "update");
                        }
                    });
        } else {
            messagingTemplate.convertAndSend("/topic/events", "update");
        }
    }

    @Transactional(readOnly = true)
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
                    .email(p.getEmail())
                    .phoneNumber(p.getPhoneNumber())
                    .website(p.getWebsite())
                    .totalLikes(0)
                    .totalDislikes(0)
                    .usageCount(0)
                    .dietaryPreferences(new java.util.HashSet<>())
                    .build());

            int eventCount = (p.getDinnerEvents() != null) ? p.getDinnerEvents().size() : 0;
            dto.setUsageCount(dto.getUsageCount() + eventCount);

            if (p.getRatings() != null) {
                for (ProposalRating r : p.getRatings()) {
                    if (r.isLiked())
                        dto.setTotalLikes(dto.getTotalLikes() + 1);
                    else
                        dto.setTotalDislikes(dto.getTotalDislikes() + 1);
                }
            }

            // Update details if present in current proposal instance (assuming latest is best, or merge logic)
            if (p.getDescription() != null && !p.getDescription().isBlank()) {
                dto.setDescription(p.getDescription());
            }
            if (p.getEmail() != null && !p.getEmail().isBlank()) dto.setEmail(p.getEmail());
            if (p.getPhoneNumber() != null && !p.getPhoneNumber().isBlank()) dto.setPhoneNumber(p.getPhoneNumber());
            if (p.getWebsite() != null && !p.getWebsite().isBlank()) dto.setWebsite(p.getWebsite());
            
            if (p.getDietaryPreferences() != null) {
                dto.getDietaryPreferences().addAll(p.getDietaryPreferences());
            }

            map.put(key, dto);
        }

        suggestions.addAll(map.values());
        suggestions.sort((a, b) -> {
            int likeCompare = Long.compare(b.getTotalLikes(), a.getTotalLikes());
            if (likeCompare != 0)
                return likeCompare;
            return Integer.compare(b.getUsageCount(), a.getUsageCount());
        });

        tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();
        for (ProposalSuggestionDTO suggestion : suggestions) {
            try {
                String json = mapper.writeValueAsString(suggestion);
                String encoded = java.util.Base64.getEncoder().encodeToString(json.getBytes());
                suggestion.setEncodedData(encoded);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return suggestions;
    }
}
