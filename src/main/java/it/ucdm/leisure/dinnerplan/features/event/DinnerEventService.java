package it.ucdm.leisure.dinnerplan.features.event;

import it.ucdm.leisure.dinnerplan.features.user.Role;
import it.ucdm.leisure.dinnerplan.features.user.User;
import it.ucdm.leisure.dinnerplan.features.user.UserRepository;
import it.ucdm.leisure.dinnerplan.features.user.UserService;
import it.ucdm.leisure.dinnerplan.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DinnerEventService {

    private final DinnerEventRepository dinnerEventRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final EmailService emailService;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public DinnerEventService(DinnerEventRepository dinnerEventRepository, UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate, UserService userService, EmailService emailService) {
        this.dinnerEventRepository = dinnerEventRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
        this.emailService = emailService;
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
        return dinnerEventRepository.findById(Objects.requireNonNull(id, "ID must not be null"))
                .orElseThrow(() -> new it.ucdm.leisure.dinnerplan.exception.ResourceNotFoundException(
                        "Invalid event Id:" + id));
    }

    @Transactional
    public DinnerEvent createEvent(String title, String description, LocalDateTime deadline, String username,
            List<Long> participantIds) {
        Objects.requireNonNull(title, "Title must not be null");
        Objects.requireNonNull(deadline, "Deadline must not be null");
        Objects.requireNonNull(username, "Username must not be null");

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

        DinnerEvent saved = dinnerEventRepository.save(Objects.requireNonNull(event));

        messagingTemplate.convertAndSendToUser(Objects.requireNonNull(organizer.getUsername()),
                "/topic/dashboard-updates", "REFRESH");
        if (participants != null) {
            for (User p : participants) {
                messagingTemplate.convertAndSendToUser(Objects.requireNonNull(p.getUsername()),
                        "/topic/dashboard-updates", "REFRESH");

                // Send Email Notification
                if (p.getEmail() != null && !p.getEmail().isEmpty()) {
                    String subject = "Sei stato invitato a un nuovo evento!";
                    String text = "Ciao " + p.getUsername() + ",\n\n" +
                            "Sei stato invitato all'evento: " + title + "\n" +
                            "Organizzato da: " + organizer.getUsername() + "\n" +
                            "Scadenza sondaggio: " + deadline + "\n\n" +
                            "Accedi all'app per votare: " + baseUrl;
                    emailService.sendSimpleMessage(p.getEmail(), subject, text);
                }
            }
        }
        return saved;
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
            newParticipants = userRepository.findAllById(participantIds);
        }

        removed.removeAll(newParticipants);

        Set<User> added = new HashSet<>(newParticipants);
        added.removeAll(currentParticipants);

        event.setParticipants(newParticipants);
        dinnerEventRepository.save(event);

        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        for (User u : added) {
                            messagingTemplate.convertAndSendToUser(Objects.requireNonNull(u.getUsername()),
                                    "/topic/dashboard-updates",
                                    "REFRESH");
                        }
                        for (User u : removed) {
                            messagingTemplate.convertAndSendToUser(Objects.requireNonNull(u.getUsername()),
                                    "/topic/dashboard-updates",
                                    "REFRESH");
                        }
                        messagingTemplate.convertAndSend("/topic/events/" + eventId, "update-participants");
                    }
                });
    }

    @Transactional
    public void deleteEvent(Long eventId, String username) {
        DinnerEvent event = dinnerEventRepository.findById(Objects.requireNonNull(eventId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid event Id:" + eventId));

        if (!event.getOrganizer().getUsername().equals(username)
                && userService.findByUsername(username).getRole() != Role.ADMIN) {
            throw new SecurityException("Only organizer or admin can delete event");
        }

        // Manually clear associations that might cause FK constraints if not handled by cascade
        event.getProposals().clear();
        event.getParticipants().clear();
        event.setSelectedProposalDate(null);
        
        dinnerEventRepository.save(event);
        dinnerEventRepository.delete(event);

        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        messagingTemplate.convertAndSend("/topic/events/" + eventId, "DELETED");
                        messagingTemplate.convertAndSend("/topic/dashboard-updates", "REFRESH");
                    }
                });
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void closeExpiredEvents() {
        List<DinnerEvent> expiredEvents = dinnerEventRepository.findByStatusAndDeadlineBefore(
                DinnerEvent.EventStatus.OPEN, LocalDateTime.now());

        for (DinnerEvent event : expiredEvents) {
            if (event.getSelectedProposalDate() == null) {
                event.setStatus(DinnerEvent.EventStatus.CLOSED);
                dinnerEventRepository.save(event);
                messagingTemplate.convertAndSend("/topic/events/" + event.getId(), "update");
            }
        }
        if (!expiredEvents.isEmpty()) {
            messagingTemplate.convertAndSend("/topic/dashboard-updates", "REFRESH");
        }
    }

    @Transactional
    public void extendDeadline(Long eventId, LocalDateTime newDeadline, String username) {
        DinnerEvent event = getEventById(Objects.requireNonNull(eventId));

        if (!event.getOrganizer().getUsername().equals(username)) {
            throw new SecurityException("Only organizer can extend deadline");
        }

        if (newDeadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("New deadline must be in the future");
        }

        event.setDeadline(newDeadline);
        if (event.getStatus() == DinnerEvent.EventStatus.CLOSED) {
            event.setStatus(DinnerEvent.EventStatus.OPEN);
        }
        dinnerEventRepository.save(event);

        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        messagingTemplate.convertAndSend("/topic/events/" + eventId, "update");
                        messagingTemplate.convertAndSend("/topic/dashboard-updates", "REFRESH");
                    }
                });
    }
}
