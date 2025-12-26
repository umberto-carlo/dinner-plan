package it.ucdm.leisure.dinnerplan.service;

import it.ucdm.leisure.dinnerplan.model.*;
import it.ucdm.leisure.dinnerplan.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class DinnerService {

    private final DinnerEventRepository dinnerEventRepository;
    private final ProposalRepository proposalRepository;
    private final VoteRepository voteRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public DinnerService(DinnerEventRepository dinnerEventRepository, ProposalRepository proposalRepository,
            VoteRepository voteRepository, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.dinnerEventRepository = dinnerEventRepository;
        this.proposalRepository = proposalRepository;
        this.voteRepository = voteRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public List<DinnerEvent> getAllEvents() {
        return dinnerEventRepository.findAllByOrderByDeadlineDesc();
    }

    public DinnerEvent getEventById(Long id) {
        return dinnerEventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id:" + id));
    }

    @Transactional
    public DinnerEvent createEvent(String title, String description, LocalDateTime deadline, String username) {
        User organizer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DinnerEvent event = DinnerEvent.builder()
                .title(title)
                .description(description)
                .deadline(deadline)
                .organizer(organizer)
                .status(DinnerEvent.EventStatus.OPEN)
                .build();

        DinnerEvent saved = dinnerEventRepository.save(event);
        messagingTemplate.convertAndSend("/topic/events", "update");
        return saved;
    }

    @Transactional
    public void addProposal(Long eventId, LocalDateTime dateOption, String location, String address,
            String description) {
        DinnerEvent event = getEventById(eventId);

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
        proposalRepository.save(proposal);
        messagingTemplate.convertAndSend("/topic/events/" + eventId, "update");
    }

    @Transactional
    public void castVote(Long proposalId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid proposal Id"));

        // basic check to see if user already voted for this proposal (optional, usually
        // can only vote once per proposal, or once per event?)
        // The prompt says "partecipanti potranno votare per uno o più soluzioni
        // proposte".
        // The unique constraint in Vote entity ensures (user, proposal) is unique.

        // We could also check deadline here.
        // We could also check deadline here.
        if (LocalDateTime.now().isAfter(proposal.getDinnerEvent().getDeadline())) {
            throw new IllegalStateException("Voting is closed");
        }

        if (proposal.getDinnerEvent().getStatus() == DinnerEvent.EventStatus.DECIDED) {
            throw new IllegalStateException("Event is already decided");
        }

        Optional<Vote> existingVote = voteRepository.findByUserAndProposal(user, proposal);

        if (existingVote.isPresent()) {
            voteRepository.delete(existingVote.get());
        } else {
            Vote vote = Vote.builder()
                    .user(user)
                    .proposal(proposal)
                    .build();

            voteRepository.save(vote);
        }
        messagingTemplate.convertAndSend("/topic/events/" + proposal.getDinnerEvent().getId(), "update");
    }

    public List<Vote> getUserVotesForEvent(Long eventId, Long userId) {
        return voteRepository.findByProposal_DinnerEvent_IdAndUser_Id(eventId, userId);
    }

    @Transactional
    public void selectProposal(Long eventId, Long proposalId, String username) {
        DinnerEvent event = dinnerEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id"));

        if (!event.getOrganizer().getUsername().equals(username)) {
            throw new IllegalStateException("Only organizer can decide the event");
        }

        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid proposal Id"));

        if (!proposal.getDinnerEvent().getId().equals(eventId)) {
            throw new IllegalArgumentException("Proposal does not belong to this event");
        }

        event.setSelectedProposal(proposal);
        event.setStatus(DinnerEvent.EventStatus.DECIDED);
        dinnerEventRepository.save(event);

        messagingTemplate.convertAndSend("/topic/events/" + eventId, "update");
        messagingTemplate.convertAndSend("/topic/events", "update"); // Update dashboard status too
    }

    public List<Proposal> getRecentUniqueProposals() {
        List<Proposal> candidates = proposalRepository.findTop50ByOrderByDinnerEvent_DeadlineDesc();
        List<Proposal> uniqueProposals = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();

        for (Proposal p : candidates) {
            // Key based on location and address (trimmed and lowercased for better
            // matching, though simple is fine)
            String key = (p.getLocation() + "|" + (p.getAddress() != null ? p.getAddress() : "")).toLowerCase();
            if (!seenKeys.contains(key)) {
                seenKeys.add(key);
                uniqueProposals.add(p);
            }
        }
        return uniqueProposals;
    }
}
