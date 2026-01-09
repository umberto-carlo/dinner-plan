package it.ucdm.leisure.dinnerplan.features.event;

import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import it.ucdm.leisure.dinnerplan.persistence.DinnerEventRepositoryPort;
import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.persistence.UserRepositoryPort;
import it.ucdm.leisure.dinnerplan.features.user.Role;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class DinnerEventService {

    private final DinnerEventRepositoryPort dinnerEventRepository;
    private final UserRepositoryPort userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public DinnerEventService(DinnerEventRepositoryPort dinnerEventRepository, UserRepositoryPort userRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.dinnerEventRepository = dinnerEventRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public List<DinnerEvent> getEventsForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return dinnerEventRepository.findDistinctByOrganizerOrParticipantsContainsOrderByDeadlineDesc(user, user);
    }

    public DinnerEvent createEvent(String username, String title, String description, LocalDateTime deadline,
            List<Long> participantIds) {
        if (deadline.isBefore(LocalDateTime.now())) {
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                    .ofPattern("dd/MM/yyyy HH:mm");
            throw new IllegalArgumentException(
                    "La scadenza deve essere una data futura (inserito: " + deadline.format(formatter) + ")");
        }

        User organizer = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<User> participants = new ArrayList<>();
        if (participantIds != null && !participantIds.isEmpty()) {
            participants = participantIds.stream()
                    .map(id -> userRepository.findById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        DinnerEvent event = DinnerEvent.builder()
                .title(title)
                .description(description)
                .deadline(deadline)
                .organizer(organizer)
                .participants(participants)
                .status(DinnerEvent.EventStatus.OPEN)
                .build();

        DinnerEvent saved = dinnerEventRepository.save(Objects.requireNonNull(event));

        if (organizer.getUsername() != null) {
            messagingTemplate.convertAndSendToUser(organizer.getUsername(), "/topic/dashboard-updates", "REFRESH");
        }
        if (participants != null) {
            for (User p : participants) {
                if (p.getUsername() != null) {
                    messagingTemplate.convertAndSendToUser(p.getUsername(), "/topic/dashboard-updates", "REFRESH");
                }
            }
        }
        return saved;
    }

    public DinnerEvent getEventById(Long id) {
        return dinnerEventRepository.findById(Objects.requireNonNull(id, "ID must not be null"))
                .orElseThrow(() -> new it.ucdm.leisure.dinnerplan.exception.ResourceNotFoundException(
                        "Invalid event Id:" + id));
    }

    @Transactional
    public void updateParticipants(Long eventId, List<Long> participantIds, String username) {
        DinnerEvent event = getEventById(Objects.requireNonNull(eventId));

        if (!event.getOrganizer().getUsername().equals(username)) {
            throw new SecurityException("Only organizer can update participants");
        }

        List<User> currentParticipants = event.getParticipants();
        Set<User> removed = new HashSet<>(currentParticipants);

        List<User> newParticipants = new ArrayList<>();
        if (participantIds != null && !participantIds.isEmpty()) {
            newParticipants = participantIds.stream()
                    .map(id -> userRepository.findById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        removed.removeAll(newParticipants); // Users in current but not in new are removed

        Set<User> added = new HashSet<>(newParticipants);
        added.removeAll(currentParticipants); // Users in new but not in current are added

        event.setParticipants(newParticipants);
        dinnerEventRepository.save(event);

        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        for (User u : added) {
                            if (u.getUsername() != null) {
                                messagingTemplate.convertAndSendToUser(u.getUsername(), "/topic/dashboard-updates",
                                        "REFRESH");
                            }
                        }
                        for (User u : removed) {
                            if (u.getUsername() != null) {
                                messagingTemplate.convertAndSendToUser(u.getUsername(), "/topic/dashboard-updates",
                                        "REFRESH");
                            }
                        }
                        messagingTemplate.convertAndSend("/topic/events/" + eventId, "update-participants");
                    }
                });
    }

    public void closeEvent(Long id) {
        DinnerEvent event = getEventById(id);
        event.setStatus(DinnerEvent.EventStatus.CLOSED);
        dinnerEventRepository.save(event);
    }

    public void checkExpiredEvents() {
        List<DinnerEvent> expiredEvents = dinnerEventRepository.findByDeadlineBeforeAndStatus(LocalDateTime.now(),
                DinnerEvent.EventStatus.OPEN);
        for (DinnerEvent event : expiredEvents) {
            event.setStatus(DinnerEvent.EventStatus.CLOSED);
            dinnerEventRepository.save(event);
        }
    }

    @Transactional
    public void deleteEvent(Long eventId, String username) {
        DinnerEvent event = dinnerEventRepository.findById(Objects.requireNonNull(eventId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id:" + eventId));

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!event.getOrganizer().getUsername().equals(username) && currentUser.getRole() != Role.ADMIN) {
            throw new SecurityException("Only organizer or admin can delete event");
        }

        dinnerEventRepository.deleteById(event.getId());

        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        messagingTemplate.convertAndSend("/topic/events/" + eventId, "DELETED");
                        messagingTemplate.convertAndSend("/topic/dashboard-updates", "REFRESH");
                    }
                });
    }
}
