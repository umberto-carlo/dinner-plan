package it.ucdm.leisure.dinnerplan.service;

import it.ucdm.leisure.dinnerplan.dto.ChatMessageDTO;
import it.ucdm.leisure.dinnerplan.model.*;
import it.ucdm.leisure.dinnerplan.repository.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class InteractionService {

    private final VoteRepository voteRepository;
    private final ProposalRepository proposalRepository;
    private final UserRepository userRepository;
    private final DinnerEventRepository dinnerEventRepository;
    private final ProposalRatingRepository proposalRatingRepository;
    private final DinnerEventMessageRepository dinnerEventMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public InteractionService(VoteRepository voteRepository, ProposalRepository proposalRepository,
            UserRepository userRepository, DinnerEventRepository dinnerEventRepository,
            ProposalRatingRepository proposalRatingRepository,
            DinnerEventMessageRepository dinnerEventMessageRepository, SimpMessagingTemplate messagingTemplate) {
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
        ProposalDate proposalDate = proposalRepository.findAll().stream()
                .flatMap(p -> p.getDates().stream())
                .filter(pd -> pd.getId().equals(proposalDateId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid proposal date Id"));

        Proposal proposal = proposalDate.getProposal();

        if (LocalDateTime.now().isAfter(proposal.getDinnerEvent().getDeadline())) {
            throw new IllegalStateException("Voting is closed");
        }

        if (proposal.getDinnerEvent().getStatus() == DinnerEvent.EventStatus.DECIDED) {
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
                        messagingTemplate.convertAndSend("/topic/events/" + proposal.getDinnerEvent().getId(),
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

        Proposal proposal = proposalDate.getProposal();

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
                || !event.getSelectedProposalDate().getProposal().getId().equals(proposalId)) {
            throw new IllegalArgumentException("You can only rate the selected proposal");
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
        return voteRepository.findByProposalDate_Proposal_DinnerEvent_IdAndUser_Id(Objects.requireNonNull(eventId),
                Objects.requireNonNull(userId));
    }

    public Optional<Boolean> getUserRatingForProposal(Long proposalId, Long userId) {
        Proposal proposal = proposalRepository.getReferenceById(Objects.requireNonNull(proposalId));
        User user = userRepository.getReferenceById(Objects.requireNonNull(userId));
        return proposalRatingRepository.findByUserAndProposal(user, proposal)
                .map(ProposalRating::isLiked);
    }

    @Transactional
    public void addMessage(Long eventId, String username, String content) {
        DinnerEvent event = dinnerEventRepository.findById(Objects.requireNonNull(eventId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id"));

        User sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isParticipant = event.getParticipants().contains(sender);
        boolean isOrganizer = event.getOrganizer().equals(sender);

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
