package it.ucdm.leisure.dinnerplan.features.event;

import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import it.ucdm.leisure.dinnerplan.model.DinnerEventMessage;
import it.ucdm.leisure.dinnerplan.model.Proposal;
import it.ucdm.leisure.dinnerplan.model.ProposalDate;
import it.ucdm.leisure.dinnerplan.model.ProposalRating;
import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.model.Vote;
import it.ucdm.leisure.dinnerplan.persistence.DinnerEventRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.ProposalRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.UserRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.VoteRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.ProposalRatingRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.DinnerEventMessageRepositoryPort;

import it.ucdm.leisure.dinnerplan.features.event.dto.ChatMessageDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class InteractionService {

    private final VoteRepositoryPort voteRepository;
    private final ProposalRepositoryPort proposalRepository;
    private final UserRepositoryPort userRepository;
    private final DinnerEventRepositoryPort dinnerEventRepository;
    private final ProposalRatingRepositoryPort proposalRatingRepository;
    private final DinnerEventMessageRepositoryPort dinnerEventMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public InteractionService(VoteRepositoryPort voteRepository, ProposalRepositoryPort proposalRepository,
            UserRepositoryPort userRepository, DinnerEventRepositoryPort dinnerEventRepository,
            ProposalRatingRepositoryPort proposalRatingRepository,
            DinnerEventMessageRepositoryPort dinnerEventMessageRepository, SimpMessagingTemplate messagingTemplate) {
        this.voteRepository = voteRepository;
        this.proposalRepository = proposalRepository;
        this.userRepository = userRepository;
        this.dinnerEventRepository = dinnerEventRepository;
        this.proposalRatingRepository = proposalRatingRepository;
        this.dinnerEventMessageRepository = dinnerEventMessageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void castVote(Long proposalDateId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Finding ProposalDate by ID. This logic is inefficient if looping all
        // proposals.
        // Assuming ProposalRepositoryPort might NOT find deep objects easily unless
        // structured differently.
        // But for now, we follow existing logic or assuming we can find it.
        // Ideally we should have findProposalDateById in repo, but strict Ports
        // architecture might limit this.
        // Given existing code looped, we loop.

        ProposalDate proposalDate = proposalRepository.findAll().stream()
                .flatMap(p -> p.getDates().stream())
                .filter(pd -> pd.getId().equals(proposalDateId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid proposal date Id"));

        // To access DinnerEvent from ProposalDate, ProposalDate domain MUST reference
        // it via Proposal?
        // Domain model ProposalDate usually doesn't back-reference DinnerEvent directly
        // in basic POJO?
        // But Proposal references DinnerEvent.
        // And ProposalDate is in Proposal.
        // We might need to find the Proposal that contains this date.

        Proposal proposalData = proposalRepository.findAll().stream()
                .filter(p -> p.getDates().stream().anyMatch(pd -> pd.getId().equals(proposalDateId)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found for date"));

        DinnerEvent event = dinnerEventRepository.findById(proposalData.getDinnerEvent().getId())
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        if (LocalDateTime.now().isAfter(event.getDeadline())) {
            throw new IllegalStateException("Voting is closed");
        }

        if (event.getStatus() == DinnerEvent.EventStatus.DECIDED) {
            throw new IllegalStateException("Event is already decided");
        }

        Optional<Vote> existingVote = voteRepository.findByUserAndProposalDate(user, proposalDate);

        if (existingVote.isPresent()) {
            voteRepository.delete(Objects.requireNonNull(existingVote.get()));
        } else {
            Vote vote = Vote.builder()
                    .user(user)
                    .proposalDate(proposalDate)
                    .build();
            voteRepository.save(Objects.requireNonNull(vote));
        }

        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        messagingTemplate.convertAndSend("/topic/events/" + event.getId(),
                                "update");
                    }
                });
    }

    @Transactional
    public void selectProposal(Long eventId, Long proposalDateId, String username) {
        DinnerEvent event = dinnerEventRepository.findById(Objects.requireNonNull(eventId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id"));

        if (!event.getOrganizer().getUsername().equals(username)) {
            throw new IllegalStateException("Only organizer can decide the event");
        }

        ProposalDate proposalDate = proposalRepository.findAll().stream()
                .flatMap(p -> p.getDates().stream())
                .filter(pd -> pd.getId().equals(proposalDateId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid proposal date Id"));

        // Verify proposal belongs to event. We need Proposal parent.
        Proposal proposal = proposalRepository.findAll().stream()
                .filter(p -> p.getDates().stream().anyMatch(pd -> pd.getId().equals(proposalDateId)))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));

        if (!proposal.getDinnerEvent().getId().equals(eventId)) {
            throw new IllegalArgumentException("Proposal does not belong to this event");
        }

        event.setSelectedProposalDate(proposalDate);
        event.setStatus(DinnerEvent.EventStatus.DECIDED);
        dinnerEventRepository.save(event);

        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        messagingTemplate.convertAndSend("/topic/events/" + eventId, "update");
                        messagingTemplate.convertAndSend("/topic/events", "update");
                    }
                });
    }

    @Transactional
    public void rateProposal(Long eventId, Long proposalId, String username, boolean isLiked) {
        DinnerEvent event = dinnerEventRepository.findById(Objects.requireNonNull(eventId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id"));

        if (event.getStatus() != DinnerEvent.EventStatus.DECIDED) {
            throw new IllegalStateException("You can only rate proposals for decided events");
        }

        if (event.getSelectedProposalDate() == null
        // Need to match Proposal of SelectedDate
        // Domain traversal might be complex if Lazy loading or pure POJO.
        // Assuming SelectedProposalDate has ref to Proposal? Or we check IDs.
        // ProposalDate domain usually doesn't link back to Proposal explicitly in some
        // designs.
        // But let's assume we can get it or check if proposalId matches.
        // Actually, logic: "You can only rate the selected proposal".
        // So proposalId must be the Proposal of the SelectedDate.
        ) {
            // We need to verify proposalId corresponds to event.getSelectedProposalDate()
            // Since ProposalDate is POJO, and doesn't explicitly store Proposal ref in code
            // I viewed...
            // Wait, I saw ProposalDate.java: " // Back reference to Proposal? Usually not
            // needed..."
            // So ProposalDate DOES NOT have getProposal().
            // We have to find Proposal by DateId or checking if Proposal contains that
            // Date.

            // Simplification: Check if the proposalId passed is indeed related to the
            // elected date.
            Proposal proposalOfSelectedDate = proposalRepository.findAll().stream()
                    .filter(p -> p.getDates().stream()
                            .anyMatch(pd -> pd.getId().equals(event.getSelectedProposalDate().getId())))
                    .findFirst().orElse(null);

            if (proposalOfSelectedDate == null || !proposalOfSelectedDate.getId().equals(proposalId)) {
                throw new IllegalArgumentException("You can only rate the selected proposal");
            }
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Proposal proposal = proposalRepository.findById(Objects.requireNonNull(proposalId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid proposal Id"));

        Optional<ProposalRating> existingRating = proposalRatingRepository.findByUserAndProposal(user, proposal);

        if (existingRating.isPresent()) {
            ProposalRating rating = existingRating.get();
            if (rating.isLiked() == isLiked) {
                proposalRatingRepository.delete(rating);
            } else {
                rating.setLiked(isLiked);
                proposalRatingRepository.save(Objects.requireNonNull(rating));
            }
        } else {
            ProposalRating rating = ProposalRating.builder()
                    .user(user)
                    .proposal(proposal)
                    .isLiked(isLiked)
                    .build();
            proposalRatingRepository.save(Objects.requireNonNull(rating));
        }
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        messagingTemplate.convertAndSend("/topic/events/" + eventId, "update");
                    }
                });
    }

    public List<Vote> getUserVotesForEvent(Long eventId, Long userId) {
        return voteRepository.findByProposalDate_DinnerEvent_IdAndUser_Id(Objects.requireNonNull(eventId),
                Objects.requireNonNull(userId));
    }

    public Optional<Boolean> getUserRatingForProposal(Long proposalId, Long userId) {
        Proposal proposal = proposalRepository.findById(Objects.requireNonNull(proposalId))
                .orElseThrow(() -> new IllegalArgumentException("Proposal not found"));
        User user = userRepository.findById(Objects.requireNonNull(userId))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return proposalRatingRepository.findByUserAndProposal(user, proposal)
                .map(ProposalRating::isLiked);
    }

    @Transactional
    public void addMessage(Long eventId, String username, String content) {
        DinnerEvent event = dinnerEventRepository.findById(Objects.requireNonNull(eventId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id"));

        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isParticipant = event.getParticipants().stream().anyMatch(u -> u.getUsername().equals(username));
        boolean isOrganizer = event.getOrganizer().getUsername().equals(username);

        if (!isParticipant && !isOrganizer) {
            throw new SecurityException("User is not a participant of this event");
        }

        DinnerEventMessage message = new DinnerEventMessage();
        message.setEvent(event);
        message.setSender(sender);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());

        DinnerEventMessage saved = dinnerEventMessageRepository.save(Objects.requireNonNull(message));

        ChatMessageDTO dto = new ChatMessageDTO(
                saved.getId(),
                saved.getSender().getUsername(),
                saved.getContent(),
                saved.getTimestamp().format(DateTimeFormatter.ofPattern("dd MMM HH:mm")));
        messagingTemplate.convertAndSend("/topic/events/" + eventId + "/chat", dto);
    }

    public List<DinnerEventMessage> getEventMessages(Long eventId) {
        return dinnerEventMessageRepository.findByEventIdOrderByTimestampAsc(eventId);
    }
}
