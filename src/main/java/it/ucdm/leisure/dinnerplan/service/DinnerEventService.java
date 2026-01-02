package it.ucdm.leisure.dinnerplan.service;

import it.ucdm.leisure.dinnerplan.model.*;
import it.ucdm.leisure.dinnerplan.repository.DinnerEventRepository;
import it.ucdm.leisure.dinnerplan.repository.UserRepository;
import it.ucdm.leisure.dinnerplan.repository.ProposalRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DinnerEventService {

    private final DinnerEventRepository dinnerEventRepository;
    private final UserRepository userRepository;
    private final ProposalRepository proposalRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public DinnerEventService(DinnerEventRepository dinnerEventRepository, UserRepository userRepository,
            ProposalRepository proposalRepository, SimpMessagingTemplate messagingTemplate) {
        this.dinnerEventRepository = dinnerEventRepository;
        this.userRepository = userRepository;
        this.proposalRepository = proposalRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public List<DinnerEvent> getEventsForUser(String username) {
        if (username == null) {
            return new ArrayList<>();
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            return dinnerEventRepository.findAllByOrderByDeadlineDesc();
        }

        return dinnerEventRepository.findDistinctByOrganizerOrParticipantsContainsOrderByDeadlineDesc(user, user);
    }

    public DinnerEvent getEventById(Long id) {
        return dinnerEventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id:" + id));
    }

    @Transactional
    public DinnerEvent createEvent(String title, String description, LocalDateTime deadline, String username,
            List<Long> participantIds) {
        User organizer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<User> participants = new ArrayList<>();
        if (participantIds != null && !participantIds.isEmpty()) {
            participants = userRepository.findAllById(participantIds);
        }

        DinnerEvent event = DinnerEvent.builder()
                .title(title)
                .description(description)
                .deadline(deadline)
                .organizer(organizer)
                .participants(participants)
                .status(DinnerEvent.EventStatus.OPEN)
                .build();

        DinnerEvent saved = dinnerEventRepository.save(event);

        messagingTemplate.convertAndSendToUser(organizer.getUsername(), "/topic/dashboard-updates", "REFRESH");
        if (participants != null) {
            for (User p : participants) {
                messagingTemplate.convertAndSendToUser(p.getUsername(), "/topic/dashboard-updates", "REFRESH");
            }
        }
        return saved;
    }

    @Transactional
    public void updateParticipants(Long eventId, List<Long> participantIds, String username) {
        DinnerEvent event = getEventById(eventId);

        if (!event.getOrganizer().getUsername().equals(username)) {
            throw new IllegalStateException("Only organizer can update participants");
        }

        if (event.getStatus() == DinnerEvent.EventStatus.DECIDED) {
            throw new IllegalStateException("Cannot update participants for a decided event");
        }

        List<User> newParticipants = new ArrayList<>();
        if (participantIds != null && !participantIds.isEmpty()) {
            newParticipants = userRepository.findAllById(participantIds);
        }

        Set<User> oldParticipantsSet = new HashSet<>(event.getParticipants());
        Set<User> newParticipantsSet = new HashSet<>(newParticipants);

        Set<User> added = new HashSet<>(newParticipantsSet);
        added.removeAll(oldParticipantsSet);

        Set<User> removed = new HashSet<>(oldParticipantsSet);
        removed.removeAll(newParticipantsSet);

        event.setParticipants(newParticipants);
        dinnerEventRepository.save(event);

        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        for (User u : added) {
                            messagingTemplate.convertAndSendToUser(u.getUsername(), "/topic/dashboard-updates",
                                    "REFRESH");
                        }
                        for (User u : removed) {
                            messagingTemplate.convertAndSendToUser(u.getUsername(), "/topic/dashboard-updates",
                                    "REFRESH");
                        }
                        messagingTemplate.convertAndSend("/topic/events/" + eventId, "update-participants");
                    }
                });
    }

    @Transactional
    public void deleteEvent(Long eventId, String username) {
        DinnerEvent event = getEventById(eventId);

        if (!event.getOrganizer().getUsername().equals(username)) {
            throw new SecurityException("Only organizer can delete event");
        }

        // Keep Proposals logic
        List<Proposal> proposals = new ArrayList<>(event.getProposals());
        for (Proposal p : proposals) {
            p.setDinnerEvent(null);
        }
        proposalRepository.saveAll(proposals);

        event.getProposals().clear();

        final String organizerUsername = event.getOrganizer().getUsername();
        final List<String> participantUsernames = event.getParticipants().stream().map(User::getUsername).toList();

        dinnerEventRepository.delete(event);

        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        messagingTemplate.convertAndSend("/topic/events/" + eventId, "DELETED");
                        messagingTemplate.convertAndSendToUser(organizerUsername, "/topic/dashboard-updates",
                                "REFRESH");
                        for (String pUsername : participantUsernames) {
                            messagingTemplate.convertAndSendToUser(pUsername, "/topic/dashboard-updates", "REFRESH");
                        }
                    }
                });
    }
}
