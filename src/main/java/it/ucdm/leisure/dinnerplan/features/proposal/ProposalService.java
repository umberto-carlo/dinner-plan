package it.ucdm.leisure.dinnerplan.features.proposal;

import it.ucdm.leisure.dinnerplan.features.event.DinnerEvent;
import it.ucdm.leisure.dinnerplan.features.event.DinnerEventRepository;
import it.ucdm.leisure.dinnerplan.features.geocode.Coordinates;
import it.ucdm.leisure.dinnerplan.features.geocode.GeocodingService;
import it.ucdm.leisure.dinnerplan.features.proposal.dto.ProposalSuggestionDTO;
import it.ucdm.leisure.dinnerplan.features.user.DietaryPreference;
import it.ucdm.leisure.dinnerplan.features.user.User;
import it.ucdm.leisure.dinnerplan.features.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProposalService {

    private static final Logger logger = LoggerFactory.getLogger(ProposalService.class);
    private final ProposalRepository proposalRepository;
    private final DinnerEventRepository dinnerEventRepository;
    private final UserRepository userRepository; // Needed to save user coordinates
    private final GeocodingService geocodingService;
    private final SimpMessagingTemplate messagingTemplate;
    private final tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();

    public ProposalService(ProposalRepository proposalRepository, DinnerEventRepository dinnerEventRepository,
                           UserRepository userRepository, GeocodingService geocodingService, SimpMessagingTemplate messagingTemplate) {
        this.proposalRepository = proposalRepository;
        this.dinnerEventRepository = dinnerEventRepository;
        this.userRepository = userRepository;
        this.geocodingService = geocodingService;
        this.messagingTemplate = messagingTemplate;
    }

    public List<Proposal> getProposalsForEvent(Long eventId) {
        return proposalRepository.findAllByDinnerEventsId(eventId);
    }

    @Transactional
    public void addCentralProposal(Long eventId, List<LocalDateTime> dateOptions) {
        DinnerEvent event = dinnerEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id:" + eventId));

        if (event.getStatus() == DinnerEvent.EventStatus.DECIDED) {
            throw new IllegalStateException("Event is already decided");
        }

        List<User> usersToConsider = new ArrayList<>(event.getParticipants());
        usersToConsider.add(event.getOrganizer());

        Set<DietaryPreference> requiredPreferences = usersToConsider.stream()
                .map(User::getDietaryPreference)
                .filter(pref -> pref != DietaryPreference.OMNIVORE)
                .collect(Collectors.toSet());

        List<Coordinates> coordinates = new ArrayList<>();
        for (User user : usersToConsider) {
            if (user.getAddress() != null && !user.getAddress().isBlank()) {
                if (user.getLatitude() != null && user.getLongitude() != null) {
                    logger.debug("Using cached coordinates for user {}", user.getUsername());
                    coordinates.add(new Coordinates(user.getLatitude(), user.getLongitude()));
                } else {
                    logger.info("Geocoding address for user {}: {}", user.getUsername(), user.getAddress());
                    try {
                        Coordinates userCoordinates = geocodingService.getCoordinates(user.getAddress());
                        if (userCoordinates != null) {
                            coordinates.add(userCoordinates);
                            try {
                                User persistentUser = userRepository.findById(user.getId()).orElse(user);
                                persistentUser.setLatitude(userCoordinates.getLatitude());
                                persistentUser.setLongitude(userCoordinates.getLongitude());
                                userRepository.save(persistentUser);
                                logger.info("Saved coordinates for user {}", user.getUsername());
                            } catch (Exception e) {
                                logger.error("Failed to save coordinates for user {}", user.getUsername(), e);
                            }
                        } else {
                            logger.warn("Could not geocode address for user {}: {}", user.getUsername(), user.getAddress());
                        }
                    } catch (Exception e) {
                        logger.error("Unexpected error geocoding user {}: {}", user.getUsername(), user.getAddress(), e);
                    }
                }
            }
        }

        if (coordinates.size() < 2) {
            logger.warn("Not enough users with valid addresses to find a central point for eventId: {}", eventId);
            return;
        }

        double lat = 0;
        double lon = 0;
        for (Coordinates c : coordinates) {
            lat += c.getLatitude();
            lon += c.getLongitude();
        }
        Coordinates center = new Coordinates(lat / coordinates.size(), lon / coordinates.size());
        logger.info("Calculated center point for eventId {}: {}", eventId, center);

        List<Proposal> candidateProposals = proposalRepository.findAll().stream()
                .filter(p -> p.getDinnerEvents().stream().noneMatch(e -> e.getId().equals(eventId)))
                .toList();

        if (candidateProposals.isEmpty()) {
            logger.warn("No candidate proposals found to add to eventId: {}", eventId);
            return;
        }

        Proposal bestProposal = null;
        double minDistance = Double.MAX_VALUE;
        long maxSatisfiedPreferences = -1;

        for (Proposal proposal : candidateProposals) {
            if (proposal.getAddress() == null || proposal.getAddress().isBlank()) {
                continue;
            }

            long satisfiedCount = 0;
            if (requiredPreferences.isEmpty()) {
                satisfiedCount = Long.MAX_VALUE;
            } else {
                satisfiedCount = requiredPreferences.stream()
                        .filter(pref -> proposal.getDietaryPreferences().contains(pref))
                        .count();
            }

            if (maxSatisfiedPreferences != -1 && satisfiedCount < maxSatisfiedPreferences) {
                continue;
            }

            Coordinates proposalCoordinates = null;
            
            if (proposal.getLatitude() != null && proposal.getLongitude() != null) {
                logger.debug("Using cached coordinates for proposal {}", proposal.getLocation());
                proposalCoordinates = new Coordinates(proposal.getLatitude(), proposal.getLongitude());
            } else {
                logger.info("Geocoding address for proposal {}: {}", proposal.getLocation(), proposal.getAddress());
                try {
                    proposalCoordinates = geocodingService.getCoordinates(proposal.getAddress());
                    if (proposalCoordinates != null) {
                        try {
                            proposal.setLatitude(proposalCoordinates.getLatitude());
                            proposal.setLongitude(proposalCoordinates.getLongitude());
                            proposalRepository.save(proposal);
                            logger.info("Saved coordinates for proposal {}", proposal.getLocation());
                        } catch (Exception e) {
                            logger.error("Failed to save coordinates for proposal {}", proposal.getLocation(), e);
                        }
                    } else {
                        logger.warn("Could not geocode address for proposal {}: {}", proposal.getLocation(), proposal.getAddress());
                    }
                } catch (Exception e) {
                    logger.error("Unexpected error geocoding proposal {}: {}", proposal.getLocation(), proposal.getAddress(), e);
                }
            }
            
            if (proposalCoordinates != null) {
                double distance = calculateHaversineDistance(
                        center.getLatitude(), center.getLongitude(),
                        proposalCoordinates.getLatitude(), proposalCoordinates.getLongitude()
                );
                
                logger.debug("Distance from center to proposal '{}' is {} km. Satisfied prefs: {}", 
                        proposal.getLocation(), distance, satisfiedCount);

                if (satisfiedCount > maxSatisfiedPreferences) {
                    maxSatisfiedPreferences = satisfiedCount;
                    minDistance = distance;
                    bestProposal = proposal;
                } else if (satisfiedCount == maxSatisfiedPreferences) {
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestProposal = proposal;
                    }
                }
            }
        }

        if (bestProposal != null) {
            logger.info("Found best proposal '{}' ({} km away, satisfied prefs: {}) for eventId: {}", 
                    bestProposal.getLocation(), String.format("%.2f", minDistance), maxSatisfiedPreferences, eventId);
            addProposal(eventId, dateOptions, bestProposal.getLocation(), bestProposal.getAddress(), bestProposal.getDescription(), bestProposal.getEmail(), bestProposal.getPhoneNumber(), bestProposal.getWebsite(), bestProposal.getDietaryPreferences());
        } else {
            logger.warn("Could not find any suitable central proposal for eventId: {}", eventId);
        }
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    @Transactional
    public void addProposal(Long eventId, List<LocalDateTime> dateOptions, String location, String address,
            String description, String email, String phoneNumber, String website, Set<DietaryPreference> dietaryPreferences) {
        Objects.requireNonNull(eventId, "Event ID must not be null");
        DinnerEvent event = dinnerEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id:" + eventId));

        if (event.getStatus() == DinnerEvent.EventStatus.DECIDED) {
            throw new IllegalStateException("Event is already decided");
        }

        Proposal proposal = proposalRepository.findByLocationIgnoreCaseAndAddressIgnoreCase(location, address)
                .orElse(null);

        if (proposal == null) {
            proposal = Proposal.builder()
                    .dinnerEvents(new ArrayList<>(List.of(event)))
                    .location(location)
                    .address(address)
                    .description(description)
                    .email(email)
                    .phoneNumber(phoneNumber)
                    .website(website)
                    .dates(new ArrayList<>())
                    .dietaryPreferences(dietaryPreferences != null ? dietaryPreferences : new java.util.HashSet<>())
                    .build();
            
            if (address != null && !address.isBlank()) {
                try {
                    Coordinates coords = geocodingService.getCoordinates(address);
                    if (coords != null) {
                        proposal.setLatitude(coords.getLatitude());
                        proposal.setLongitude(coords.getLongitude());
                    }
                } catch (Exception e) {
                    logger.error("Unexpected error geocoding new proposal {}: {}", location, address, e);
                }
            }
            
            event.getProposals().add(proposal);
        } else {
            if (!proposal.getDinnerEvents().contains(event)) {
                proposal.getDinnerEvents().add(event);
                event.getProposals().add(proposal);
            }
            
            if (proposal.getAddress() != null && !proposal.getAddress().isBlank() && 
                (proposal.getLatitude() == null || proposal.getLongitude() == null)) {
                try {
                    Coordinates coords = geocodingService.getCoordinates(proposal.getAddress());
                    if (coords != null) {
                        proposal.setLatitude(coords.getLatitude());
                        proposal.setLongitude(coords.getLongitude());
                    }
                } catch (Exception e) {
                    logger.error("Unexpected error geocoding existing proposal {}: {}", location, address, e);
                }
            }

            if ((proposal.getDescription() == null || proposal.getDescription().isBlank()) && description != null
                    && !description.isBlank()) {
                proposal.setDescription(description);
            }
            if ((proposal.getEmail() == null || proposal.getEmail().isBlank()) && email != null && !email.isBlank()) {
                proposal.setEmail(email);
            }
            if ((proposal.getPhoneNumber() == null || proposal.getPhoneNumber().isBlank()) && phoneNumber != null && !phoneNumber.isBlank()) {
                proposal.setPhoneNumber(phoneNumber);
            }
            if ((proposal.getWebsite() == null || proposal.getWebsite().isBlank()) && website != null && !website.isBlank()) {
                proposal.setWebsite(website);
            }
            
            if (dietaryPreferences != null && !dietaryPreferences.isEmpty()) {
                proposal.setDietaryPreferences(dietaryPreferences);
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
    public void addProposal(Long eventId, List<LocalDateTime> dateOptions, String location, String address,
            String description, Set<DietaryPreference> dietaryPreferences) {
        addProposal(eventId, dateOptions, location, address, description, null, null, null, dietaryPreferences);
    }

    @Transactional
    public void addProposal(Long eventId, List<LocalDateTime> dateOptions, String location, String address,
            String description) {
        addProposal(eventId, dateOptions, location, address, description, null, null, null, null);
    }

    @Transactional
    public void addProposalFromSuggestion(Long eventId, List<LocalDateTime> dateOptions, String location,
            String address,
            String description, String email, String phoneNumber, String website, String username) {
        Objects.requireNonNull(eventId, "Event ID must not be null");
        DinnerEvent event = dinnerEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id"));

        if (!event.getOrganizer().getUsername().equals(username)) {
            throw new SecurityException("Only organizer can add proposals");
        }

        if (event.getStatus() == DinnerEvent.EventStatus.DECIDED) {
            throw new IllegalStateException("Event is already decided");
        }

        addProposal(eventId, dateOptions, location, address, description, email, phoneNumber, website, null);
    }
    
    @Transactional
    public void addProposalFromSuggestion(Long eventId, List<LocalDateTime> dateOptions, String location,
            String address,
            String description, String username) {
        addProposalFromSuggestion(eventId, dateOptions, location, address, description, null, null, null, username);
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

                addProposal(eventId, List.of(date), dto.getLocation(), dto.getAddress(), dto.getDescription(), dto.getEmail(), dto.getPhoneNumber(), dto.getWebsite(), dto.getDietaryPreferences());
                addedCount++;
            } catch (Exception e) {
                logger.error("Error processing batch proposal", e);
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
    
    @Transactional
    public void updateProposalDietaryPreferences(Long proposalId, Set<DietaryPreference> preferences) {
        Proposal proposal = proposalRepository.findById(Objects.requireNonNull(proposalId))
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        proposal.setDietaryPreferences(preferences != null ? preferences : new java.util.HashSet<>());
        proposalRepository.save(proposal);
    }
}
