package it.ucdm.leisure.dinnerplan.features.event;

import it.ucdm.leisure.dinnerplan.features.event.dto.CalendarEventDTO;
import it.ucdm.leisure.dinnerplan.features.event.dto.SmartEventRequest;
import it.ucdm.leisure.dinnerplan.features.proposal.Proposal;
import it.ucdm.leisure.dinnerplan.features.proposal.ProposalCatalogService;
import it.ucdm.leisure.dinnerplan.features.proposal.ProposalService;
import it.ucdm.leisure.dinnerplan.features.user.DietaryPreference;
import it.ucdm.leisure.dinnerplan.features.user.Role;
import it.ucdm.leisure.dinnerplan.features.user.User;
import it.ucdm.leisure.dinnerplan.features.user.UserService;
import it.ucdm.leisure.dinnerplan.utils.UserAgentUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class DinnerController {

    private final DinnerEventService dinnerEventService;
    private final ProposalService proposalService;
    private final ProposalCatalogService proposalCatalogService;
    private final InteractionService interactionService;
    private final UserService userService;
    private final UserAgentUtils userAgentUtils;

    public DinnerController(DinnerEventService dinnerEventService, ProposalService proposalService,
            ProposalCatalogService proposalCatalogService, InteractionService interactionService,
            UserService userService, UserAgentUtils userAgentUtils) {
        this.dinnerEventService = dinnerEventService;
        this.proposalService = proposalService;
        this.proposalCatalogService = proposalCatalogService;
        this.interactionService = interactionService;
        this.userService = userService;
        this.userAgentUtils = userAgentUtils;
    }

    @GetMapping("/manual")
    public String manual() {
        return "manual";
    }

    @GetMapping("/")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());
            List<DinnerEvent> eventList = dinnerEventService
                    .getEventsForUser(userDetails.getUsername());
            model.addAttribute("events", eventList);
            model.addAttribute("rankedProposals", proposalCatalogService.getProposalSuggestions());
            model.addAttribute("user", user);
            model.addAttribute("isOrganizer", user.getRole() == Role.ORGANIZER);
        } else {
            model.addAttribute("events", new ArrayList<>());
            model.addAttribute("rankedProposals", new ArrayList<>());
        }

        if (userAgentUtils.isMobile(userAgent)) {
            return "mobile/dashboard";
        }
        return "dashboard";
    }

    @GetMapping("/fragments/dashboard")
    public String getDashboardFragment(Model model, @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());
            model.addAttribute("events", dinnerEventService.getEventsForUser(userDetails.getUsername()));
            model.addAttribute("rankedProposals", proposalCatalogService.getProposalSuggestions());
            model.addAttribute("user", user);
            model.addAttribute("isOrganizer", user.getRole() == Role.ORGANIZER);
        } else {
            model.addAttribute("events", new ArrayList<>());
            model.addAttribute("rankedProposals", new ArrayList<>());
        }

        if (userAgentUtils.isMobile(userAgent)) {
            return "mobile/dashboard :: dashboardContent";
        }
        return "dashboard :: dashboardContent";
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping("/events/create")
    public String createEventForm(Model model,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        if (userAgentUtils.isMobile(userAgent)) {
            return "mobile/create_event";
        }
        return "create_event";
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/events/create")
    public String createEvent(@RequestParam String title, @RequestParam String description,
            @RequestParam String deadline, @RequestParam(required = false) List<Long> participantIds,
            @AuthenticationPrincipal UserDetails userDetails) {
        LocalDateTime dt = LocalDateTime.parse(deadline);
        DinnerEvent event = dinnerEventService.createEvent(title, description, dt, userDetails.getUsername(),
                participantIds);
        return "redirect:/events/" + event.getId();
    }

    @PostMapping("/events/{id}/update-participants")
    @PreAuthorize("hasRole('ORGANIZER')")
    public String updateParticipants(@PathVariable Long id, @RequestParam(required = false) List<Long> participantIds,
            @AuthenticationPrincipal UserDetails userDetails) {
        dinnerEventService.updateParticipants(id, participantIds, userDetails.getUsername());
        return "redirect:/events/" + id;
    }

    // Base population for common event data (User access, basic event info)
    private DinnerEvent populateBaseEventModel(Long id, Model model, UserDetails userDetails) {
        DinnerEvent event = dinnerEventService.getEventById(id);

        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());

            boolean isParticipant = event.getParticipants().stream().anyMatch(p -> p.getId().equals(user.getId()));
            boolean isOrganizer = event.getOrganizer().getId().equals(user.getId());
            boolean isAdmin = user.getRole() == Role.ADMIN;

            if (!isOrganizer && !isParticipant && !isAdmin) {
                return null;
            }

            model.addAttribute("event", event);
            model.addAttribute("currentUser", user);
            model.addAttribute("isOrganizer", isOrganizer);

            // Check if we can add a central proposal
            if (isOrganizer) {
                long usersWithAddressCount = Stream.concat(event.getParticipants().stream(), Stream.of(event.getOrganizer()))
                        .filter(u -> u.getAddress() != null && !u.getAddress().isBlank())
                        .distinct()
                        .count();
                model.addAttribute("canAddCentralProposal", usersWithAddressCount >= 2);
            } else {
                model.addAttribute("canAddCentralProposal", false);
            }

            return event;
        }
        return null;
    }

    // Heavy population for full page load
    private boolean populateFullEventModel(Long id, Model model, UserDetails userDetails) {
        DinnerEvent event = populateBaseEventModel(id, model, userDetails);
        if (event == null)
            return false;

        List<it.ucdm.leisure.dinnerplan.features.proposal.dto.ProposalSuggestionDTO> allSuggestions = proposalCatalogService
                .getProposalSuggestions();
        java.util.Set<String> existingKeys = event.getProposals().stream()
                .map(p -> (p.getLocation() + "|" + (p.getAddress() != null ? p.getAddress() : "")).toLowerCase())
                .collect(java.util.stream.Collectors.toSet());

        List<it.ucdm.leisure.dinnerplan.features.proposal.dto.ProposalSuggestionDTO> filteredSuggestions = allSuggestions
                .stream()
                .filter(dto -> {
                    String key = (dto.getLocation() + "|" + (dto.getAddress() != null ? dto.getAddress() : ""))
                            .toLowerCase();
                    return !existingKeys.contains(key);
                })
                .toList();

        model.addAttribute("recentProposals", filteredSuggestions);

        List<Proposal> sortedProposals = new ArrayList<>(proposalService.getProposalsForEvent(id));
        Long selectedProposalId = event.getSelectedProposalDate() != null
                ? event.getSelectedProposalDate().getProposal().getId()
                : -1L;
        sortedProposals.sort((p1, p2) -> {
            if (p1.getId().equals(selectedProposalId))
                return -1;
            if (p2.getId().equals(selectedProposalId))
                return 1;
            int v1 = p1.getDates().stream()
                    .filter(d -> d.getDinnerEvent().getId().equals(id))
                    .mapToInt(d -> d.getVotes().size()).max().orElse(0);
            int v2 = p2.getDates().stream()
                    .filter(d -> d.getDinnerEvent().getId().equals(id))
                    .mapToInt(d -> d.getVotes().size()).max().orElse(0);
            return Integer.compare(v2, v1);
        });
        model.addAttribute("sortedProposals", sortedProposals);

        User user = (User) model.getAttribute("currentUser");
        if (user == null) {
            return false;
        }

        var votes = interactionService.getUserVotesForEvent(id, user.getId());
        var votedProposalDateIds = votes.stream().map(v -> v.getProposalDate().getId()).toList();
        model.addAttribute("votedProposalDateIds", votedProposalDateIds);

        if (event.getStatus() == DinnerEvent.EventStatus.DECIDED && event.getSelectedProposalDate() != null) {
            interactionService
                    .getUserRatingForProposal(event.getSelectedProposalDate().getProposal().getId(), user.getId())
                    .ifPresent(rating -> model.addAttribute("userRating", rating));
        }

        Boolean isOrganizerObj = (Boolean) model.getAttribute("isOrganizer");
        boolean isOrganizer = Boolean.TRUE.equals(isOrganizerObj);
        if (isOrganizer) {
            model.addAttribute("allUsers", userService.getAllUsers());
        }

        model.addAttribute("chatMessages", interactionService.getEventMessages(id));

        return true;
    }

    @GetMapping("/events/{id}")
    public String eventDetails(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        if (!populateFullEventModel(id, model, userDetails)) {
            return "redirect:/";
        }
        if (userAgentUtils.isMobile(userAgent)) {
            return "mobile/event_details";
        }
        return "event_details";
    }

    @GetMapping("/events/{id}/fragments/header")
    public String getEventHeaderFragment(@PathVariable Long id, Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        if (populateBaseEventModel(id, model, userDetails) == null)
            return "redirect:/";
        if (userAgentUtils.isMobile(userAgent)) {
            return "mobile/fragments/event-header :: eventHeader";
        }
        return "fragments/event-header :: eventHeader";
    }

    @GetMapping("/events/{id}/fragments/actions")
    public String getEventActionsFragment(@PathVariable Long id, Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        if (populateBaseEventModel(id, model, userDetails) == null)
            return "redirect:/";
        if (userAgentUtils.isMobile(userAgent)) {
            return "mobile/fragments/event-actions :: eventActions";
        }
        return "fragments/event-actions :: eventActions";
    }

    @GetMapping("/events/{id}/fragments/proposals")
    public String getProposalListFragment(@PathVariable Long id, Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        DinnerEvent event = populateBaseEventModel(id, model, userDetails);
        if (event == null)
            return "redirect:/";

        List<Proposal> sortedProposals = new ArrayList<>(proposalService.getProposalsForEvent(id));
        Long selectedProposalId = event.getSelectedProposalDate() != null
                ? event.getSelectedProposalDate().getProposal().getId()
                : -1L;
        sortedProposals.sort((p1, p2) -> {
            if (p1.getId().equals(selectedProposalId))
                return -1;
            if (p2.getId().equals(selectedProposalId))
                return 1;
            int v1 = p1.getDates().stream()
                    .filter(d -> d.getDinnerEvent().getId().equals(id))
                    .mapToInt(d -> d.getVotes().size()).max().orElse(0);
            int v2 = p2.getDates().stream()
                    .filter(d -> d.getDinnerEvent().getId().equals(id))
                    .mapToInt(d -> d.getVotes().size()).max().orElse(0);
            return Integer.compare(v2, v1);
        });
        model.addAttribute("sortedProposals", sortedProposals);

        User user = (User) model.getAttribute("currentUser");
        if (user != null) {
            var votes = interactionService.getUserVotesForEvent(id, user.getId());
            var votedProposalDateIds = votes.stream().map(v -> v.getProposalDate().getId()).toList();
            model.addAttribute("votedProposalDateIds", votedProposalDateIds);
        }

        if (event.getStatus() == DinnerEvent.EventStatus.DECIDED && event.getSelectedProposalDate() != null
                && user != null) {
            interactionService
                    .getUserRatingForProposal(event.getSelectedProposalDate().getProposal().getId(), user.getId())
                    .ifPresent(rating -> model.addAttribute("userRating", rating));
        }

        if (userAgentUtils.isMobile(userAgent)) {
            return "mobile/fragments/event-proposals :: proposalList";
        }
        return "fragments/event-proposals :: proposalList";
    }

    @GetMapping("/events/{id}/fragments/participants")
    public String getParticipantListFragment(@PathVariable Long id, Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        DinnerEvent event = populateBaseEventModel(id, model, userDetails);
        if (event == null)
            return "redirect:/";

        Boolean isOrganizerObj = (Boolean) model.getAttribute("isOrganizer");
        boolean isOrganizer = Boolean.TRUE.equals(isOrganizerObj);
        if (isOrganizer) {
            model.addAttribute("allUsers", userService.getAllUsers());
        }
        if (userAgentUtils.isMobile(userAgent)) {
            return "mobile/event_details :: participantLists";
        }
        return "fragments/event-participants :: participantLists";
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/events/prepare-smart")
    public String prepareSmartEvent(@RequestParam(required = false) List<String> selectedProposals, Model model,
            @RequestHeader(value = "User-Agent", required = false) String userAgent) {
        if (selectedProposals == null || selectedProposals.isEmpty()) {
            return "redirect:/";
        }

        List<it.ucdm.leisure.dinnerplan.features.proposal.dto.ProposalSuggestionDTO> proposals = new ArrayList<>();
        tools.jackson.databind.ObjectMapper mapper = new tools.jackson.databind.ObjectMapper();

        for (String encoded : selectedProposals) {
            try {
                String json = new String(java.util.Base64.getDecoder().decode(encoded));
                proposals.add(mapper.readValue(json,
                        it.ucdm.leisure.dinnerplan.features.proposal.dto.ProposalSuggestionDTO.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        model.addAttribute("selectedProposals", proposals);
        if (userAgentUtils.isMobile(userAgent)) {
            return "mobile/create_smart_event";
        }
        return "create_smart_event";
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/events/create-smart")
    public String createSmartEvent(
            @ModelAttribute SmartEventRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Technically this involves creating event AND proposals.
        // DinnerEventService needs a method for this or we coordinate here.
        // I moved createEventWithProposals to DinnerService before.
        // Let's check where I put it. It was in DinnerService.
        // I put logic in DinnerEventService?
        // Let's check DinnerEventService content. I might have missed it or put it
        // there.
        // If not, I should add it to DinnerEventService or orchestrate here.
        // Ideally Service handles transaction.
        // I will assume I added it to DinnerEventService. If not, I'll add it in next
        // step.
        // Wait, looking at DinnerEventService copy I made... I don't see
        // createEventWithProposals immediately in my head.
        // I better check.
        // But for now let's call it on dinnerEventService assuming I should put it
        // there.

        // Wait, logic involves ADDING proposals.
        // So it depends on ProposalService logic potentially?
        // Or simply DinnerEventService uses repository logic.
        // Refactoring principle: Event creation with initial data is EventService
        // responsibility.
        // So I will call dinnerEventService.createEventWithProposals.
        // I need to ensure that method exists in DinnerEventService. I will check/add
        // it.

        it.ucdm.leisure.dinnerplan.features.event.DinnerEvent event = dinnerEventService.createEvent(
                request.getTitle(),
                request.getDescription(),
                LocalDateTime.parse(request.getDeadline()),
                userDetails.getUsername(),
                new ArrayList<>() // No participants initially? Smart event usually implies just creation.
        );

        // Oh, createEventWithProposals was atomic in DinnerService.
        // I should probably have duplicated/moved that logic to DinnerEventService.
        // Since I'm essentially orchestrating, I can do it here in Controller if I
        // don't want to couple services too much,
        // OR add it to DinnerEventService which then might depend on ProposalService?
        // No, DinnerEventService has ProposalRepository so it can save proposals.
        // I will implement the logic: create event, then loop and save proposals.
        // I'll assume valid service method or simple orchestration here.
        // Let's rely on `ProposalService` to add proposals since it has the logic (and
        // duplicate checks if any, though smart event usually raw).
        // Actually, `createSmartEvent` in `DinnerService` used `addProposal` internal
        // method.
        // I will inject `ProposalService` and use it here.

        if (request.getProposals() != null) {
            for (var p : request.getProposals()) {
                if (p.getDateOption() != null && !p.getDateOption().isEmpty()) {
                    proposalService.addProposal(event.getId(), List.of(LocalDateTime.parse(p.getDateOption())),
                            p.getLocation(),
                            p.getAddress(), p.getDescription());
                }
            }
        }

        return "redirect:/events/" + event.getId();
    }

    @PostMapping("/events/{id}/delete")
    @PreAuthorize("hasRole('ORGANIZER')")
    public String deleteEvent(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        dinnerEventService.deleteEvent(id, userDetails.getUsername());
        return "redirect:/";
    }

    @GetMapping("/api/calendar/events")
    public @org.springframework.web.bind.annotation.ResponseBody List<CalendarEventDTO> getCalendarEvents(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null)
            return new ArrayList<>();

        List<DinnerEvent> events = dinnerEventService.getEventsForUser(userDetails.getUsername());
        List<CalendarEventDTO> calendarEvents = new ArrayList<>();

        for (DinnerEvent event : events) {
            // Add Deadline
            calendarEvents.add(new it.ucdm.leisure.dinnerplan.features.event.dto.CalendarEventDTO(
                    event.getId(),
                    "Deadline: " + event.getTitle() + " ("
                            + event.getDeadline().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) + ")",
                    event.getDeadline(),
                    "DEADLINE",
                    "Scadenza votazioni per " + event.getTitle()));

            // Add Actual Event Date if decided
            if (event.getStatus() == DinnerEvent.EventStatus.DECIDED && event.getSelectedProposalDate() != null) {
                calendarEvents.add(new it.ucdm.leisure.dinnerplan.features.event.dto.CalendarEventDTO(
                        event.getId(),
                        "Cena: " + event.getTitle() + " ("
                                + event.getSelectedProposalDate().getDate()
                                        .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
                                + ")",
                        event.getSelectedProposalDate().getDate(),
                        "EVENT",
                        "Cena presso " + event.getSelectedProposalDate().getProposal().getLocation()));
            }
        }
        return calendarEvents;
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/proposals/{proposalId}/dates/{dateId}/delete")
    public String deleteProposalDate(@PathVariable Long proposalId, @PathVariable Long dateId,
            @RequestParam Long eventId, @AuthenticationPrincipal UserDetails userDetails) {
        proposalService.deleteProposalDate(proposalId, dateId, userDetails.getUsername());
        return "redirect:/events/" + eventId;
    }

    @PostMapping("/events/{eventId}/proposals/{proposalId}/rate")
    public String rateProposal(@PathVariable Long eventId, @PathVariable Long proposalId,
            @RequestParam boolean isLiked, @AuthenticationPrincipal UserDetails userDetails) {
        interactionService.rateProposal(eventId, proposalId, userDetails.getUsername(), isLiked);
        return "redirect:/events/" + eventId;
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/events/{id}/add-central-proposal")
    public String addCentralProposal(@PathVariable Long id,
            @RequestParam("dateOption") String dateOption,
            @AuthenticationPrincipal UserDetails userDetails) {
        proposalService.addCentralProposal(id, List.of(LocalDateTime.parse(dateOption)));
        return "redirect:/events/" + id;
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/events/{id}/extend-deadline")
    public String extendDeadline(@PathVariable Long id, @RequestParam("newDeadline") String newDeadline,
            @AuthenticationPrincipal UserDetails userDetails) {
        dinnerEventService.extendDeadline(id, LocalDateTime.parse(newDeadline), userDetails.getUsername());
        return "redirect:/events/" + id;
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/events/{id}/add-proposal")
    public String addProposal(@PathVariable Long id, @RequestParam String location, @RequestParam String address,
            @RequestParam(required = false) String description, @RequestParam String dateOption,
            @RequestParam(required = false) List<String> dietaryPreferences,
            @AuthenticationPrincipal UserDetails userDetails,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        if (dietaryPreferences == null || dietaryPreferences.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Devi selezionare almeno una preferenza alimentare.");
            return "redirect:/events/" + id;
        }

        Set<DietaryPreference> preferences = dietaryPreferences.stream()
                .map(DietaryPreference::valueOf)
                .collect(Collectors.toSet());

        proposalService.addProposal(id, List.of(LocalDateTime.parse(dateOption)), location, address, description, preferences);
        return "redirect:/events/" + id;
    }
}
