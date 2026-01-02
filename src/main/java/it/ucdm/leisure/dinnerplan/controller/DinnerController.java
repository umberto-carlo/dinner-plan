package it.ucdm.leisure.dinnerplan.controller;

import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import it.ucdm.leisure.dinnerplan.model.Role;
import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.service.DinnerService;
import it.ucdm.leisure.dinnerplan.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import it.ucdm.leisure.dinnerplan.model.Proposal;

@Controller
public class DinnerController {

    private final DinnerService dinnerService;
    private final UserService userService;

    public DinnerController(DinnerService dinnerService, UserService userService) {
        this.dinnerService = dinnerService;
        this.userService = userService;
    }

    @GetMapping("/manual")
    public String manual() {
        return "manual";
    }

    @GetMapping("/")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());
            model.addAttribute("events", dinnerService.getEventsForUser(userDetails.getUsername()));
            model.addAttribute("rankedProposals", dinnerService.getProposalSuggestions());
            model.addAttribute("user", user);
            model.addAttribute("isOrganizer", user.getRole() == Role.ORGANIZER);
        } else {
            // Default or empty for non-logged in?
            // Assuming login is required, otherwise redirect or empty.
            model.addAttribute("events", new ArrayList<>());
            model.addAttribute("rankedProposals", new ArrayList<>());
        }
        return "dashboard";
    }

    @GetMapping("/fragments/dashboard")
    public String getDashboardFragment(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            model.addAttribute("events", dinnerService.getEventsForUser(userDetails.getUsername()));
            model.addAttribute("rankedProposals", dinnerService.getProposalSuggestions());
        } else {
            model.addAttribute("events", new ArrayList<>());
            model.addAttribute("rankedProposals", new ArrayList<>());
        }
        return "dashboard :: dashboardContent";
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping("/events/create")
    public String createEventForm(Model model) {
        // model.addAttribute("users", userService.getAllUsers()); // Removed as per
        // user request (UI section removed)
        return "create_event";
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/events/create")
    public String createEvent(@RequestParam String title, @RequestParam String description,
            @RequestParam String deadline, @RequestParam(required = false) List<Long> participantIds,
            @AuthenticationPrincipal UserDetails userDetails) {
        // deadline parsed from string. assuming HTML5 datetime-local input, format:
        // yyyy-MM-ddTHH:mm
        LocalDateTime dt = LocalDateTime.parse(deadline);
        dinnerService.createEvent(title, description, dt, userDetails.getUsername(), participantIds);
        return "redirect:/";
    }

    @PostMapping("/events/{id}/update-participants")
    @PreAuthorize("hasRole('ORGANIZER')")
    public String updateParticipants(@PathVariable Long id, @RequestParam(required = false) List<Long> participantIds,
            @AuthenticationPrincipal UserDetails userDetails) {
        dinnerService.updateParticipants(id, participantIds, userDetails.getUsername());
        return "redirect:/events/" + id;
    }

    // Base population for common event data (User access, basic event info)
    private DinnerEvent populateBaseEventModel(Long id, Model model, UserDetails userDetails) {
        DinnerEvent event = null;
        try {
            event = dinnerService.getEventById(id);
        } catch (IllegalArgumentException e) {
            return null; // Event Not Found
        }

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
            return event;
        }
        return null; // Not authenticated or user not found
    }

    // Heavy population for full page load
    private boolean populateFullEventModel(Long id, Model model, UserDetails userDetails) {
        DinnerEvent event = populateBaseEventModel(id, model, userDetails);
        if (event == null)
            return false;

        // Recent proposals for suggestion (EXPENSIVE - only needed for Add Proposal
        // form)
        model.addAttribute("recentProposals", dinnerService.getProposalSuggestions());

        // Sorted Proposals
        // Sorted Proposals
        List<Proposal> sortedProposals = new ArrayList<>(dinnerService.getProposalsForEvent(id));
        sortedProposals.sort(Comparator.comparingInt((Proposal p) -> p.getVotes().size())
                .reversed()
                .thenComparing(Proposal::getDateOption));
        model.addAttribute("sortedProposals", sortedProposals);

        User user = (User) model.getAttribute("currentUser");

        // User Votes
        var votes = dinnerService.getUserVotesForEvent(id, user.getId());
        var votedProposalIds = votes.stream().map(v -> v.getProposal().getId()).toList();
        model.addAttribute("votedProposalIds", votedProposalIds);

        // User Rating
        if (event.getStatus() == DinnerEvent.EventStatus.DECIDED && event.getSelectedProposal() != null) {
            dinnerService.getUserRatingForProposal(event.getSelectedProposal().getId(), user.getId())
                    .ifPresent(rating -> model.addAttribute("userRating", rating));
        }

        // All Users for Organizer
        boolean isOrganizer = (boolean) model.getAttribute("isOrganizer");
        if (isOrganizer) {
            model.addAttribute("allUsers", userService.getAllUsers());
        }

        // Chat Messages
        model.addAttribute("chatMessages", dinnerService.getEventMessages(id));

        return true;
    }

    @GetMapping("/events/{id}")
    public String eventDetails(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (!populateFullEventModel(id, model, userDetails)) {
            return "redirect:/";
        }
        return "event_details";
    }

    @GetMapping("/events/{id}/fragments/header")
    public String getEventHeaderFragment(@PathVariable Long id, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Optimzation: Header only needs event data
        if (populateBaseEventModel(id, model, userDetails) == null)
            return "redirect:/";
        return "event_details :: eventHeader";
    }

    @GetMapping("/events/{id}/fragments/actions")
    public String getEventActionsFragment(@PathVariable Long id, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Actions needs event data (status) and isOrganizer. Base is enough.
        // NOTE: 'new proposal' button is here but the FORM (with suggestions) is hidden
        // and NOT refreshed by this fragment.
        if (populateBaseEventModel(id, model, userDetails) == null)
            return "redirect:/";
        return "event_details :: eventActions";
    }

    @GetMapping("/events/{id}/fragments/proposals")
    public String getProposalListFragment(@PathVariable Long id, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Needs Proposals, Votes, User Rating. Does NOT need Chat or Suggestions.
        DinnerEvent event = populateBaseEventModel(id, model, userDetails);
        if (event == null)
            return "redirect:/";

        // Sorted Proposals
        // Sorted Proposals
        List<Proposal> sortedProposals = new ArrayList<>(dinnerService.getProposalsForEvent(id));
        sortedProposals.sort(Comparator.comparingInt((Proposal p) -> p.getVotes().size())
                .reversed()
                .thenComparing(Proposal::getDateOption));
        model.addAttribute("sortedProposals", sortedProposals);

        User user = (User) model.getAttribute("currentUser");
        var votes = dinnerService.getUserVotesForEvent(id, user.getId());
        var votedProposalIds = votes.stream().map(v -> v.getProposal().getId()).toList();
        model.addAttribute("votedProposalIds", votedProposalIds);

        if (event.getStatus() == DinnerEvent.EventStatus.DECIDED && event.getSelectedProposal() != null) {
            dinnerService.getUserRatingForProposal(event.getSelectedProposal().getId(), user.getId())
                    .ifPresent(rating -> model.addAttribute("userRating", rating));
        }

        return "event_details :: proposalList";
    }

    @GetMapping("/events/{id}/fragments/participants")
    public String getParticipantListFragment(@PathVariable Long id, Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Needs All Users if Organizer.
        DinnerEvent event = populateBaseEventModel(id, model, userDetails);
        if (event == null)
            return "redirect:/";

        boolean isOrganizer = (boolean) model.getAttribute("isOrganizer");
        if (isOrganizer) {
            model.addAttribute("allUsers", userService.getAllUsers());
        }
        return "event_details :: participantLists";
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/events/{id}/add-proposal")
    public String addProposal(@PathVariable Long id, @RequestParam String dateOption, @RequestParam String location,
            @RequestParam String address, @RequestParam String description,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            System.out.println("DEBUG: addProposal START - ID: " + id);
            System.out.println("DEBUG: dateOption: " + dateOption);

            LocalDateTime dt = LocalDateTime.parse(dateOption);
            dinnerService.addProposal(id, dt, location, address, description);

            System.out.println("DEBUG: addProposal SUCCESS");
            return "redirect:/events/" + id;
        } catch (Exception e) {
            System.out.println("DEBUG: addProposal ERROR");
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Errore durante l'aggiunta della proposta: " + e.getMessage());
            return "redirect:/events/" + id;
        }
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/proposals/add")
    public String addGlobalProposal(@RequestParam String location, @RequestParam String address,
            @RequestParam String description,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            dinnerService.addGlobalProposal(location, address, description);
            return "redirect:/";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Errore durante l'aggiunta della proposta: " + e.getMessage());
            return "redirect:/";
        }
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/proposals/add-to-event")
    public String addSuggestionToEvent(@RequestParam Long eventId, @RequestParam String location,
            @RequestParam String address, @RequestParam String description, @RequestParam String dateOption,
            @AuthenticationPrincipal UserDetails userDetails,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            LocalDateTime dt = LocalDateTime.parse(dateOption);
            dinnerService.addProposalFromSuggestion(eventId, dt, location, address, description,
                    userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage", "Proposta aggiunta all'evento con successo.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Errore generico: " + e.getMessage());
        }
        return "redirect:/";
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/proposals/add-batch-to-event")
    public String addBatchSuggestionToEvent(@RequestParam Long eventId,
            @RequestParam("selectedProposals") List<String> selectedProposals,
            @RequestParam("dateOptions") List<String> dateOptions,
            @AuthenticationPrincipal UserDetails userDetails,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            List<LocalDateTime> dts = new ArrayList<>();
            for (String d : dateOptions) {
                if (d != null && !d.isBlank()) {
                    dts.add(LocalDateTime.parse(d));
                } else {
                    // Fallback or error? Logic assumes strict 1:1 match.
                    // Frontend 'required' should prevent empty.
                    dts.add(null);
                }
            }

            int count = dinnerService.addBatchProposalsFromSuggestion(eventId, dts, selectedProposals,
                    userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Aggiunte " + count + " proposte all'evento (i duplicati sono stati ignorati).");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Errore durante l'aggiunta multipla: " + e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/proposals/{id}/vote")
    public String vote(@PathVariable Long id, @RequestParam Long eventId,
            @AuthenticationPrincipal UserDetails userDetails) {
        dinnerService.castVote(id, userDetails.getUsername());
        return "redirect:/events/" + eventId;
    }

    @PostMapping("/events/{id}/select-proposal")
    public String selectProposal(@PathVariable Long id, @RequestParam Long proposalId,
            @AuthenticationPrincipal UserDetails userDetails) {
        dinnerService.selectProposal(id, proposalId, userDetails.getUsername());
        return "redirect:/events/" + id;
    }

    @PostMapping("/events/{id}/rate")
    public String rateProposal(@PathVariable Long id, @RequestParam Long proposalId, @RequestParam boolean isLiked,
            @AuthenticationPrincipal UserDetails userDetails) {
        dinnerService.rateProposal(id, proposalId, userDetails.getUsername(), isLiked);
        return "redirect:/events/" + id;
    }

    @PostMapping("/events/{id}/chat/send")
    @ResponseBody
    public ResponseEntity<?> sendMessage(@PathVariable Long id, @RequestParam String content,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            dinnerService.addMessage(id, userDetails.getUsername(), content);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/events/prepare-smart")
    public String prepareSmartEvent(@RequestParam(required = false) List<String> selectedProposals, Model model) {
        if (selectedProposals == null || selectedProposals.isEmpty()) {
            return "redirect:/";
        }

        List<it.ucdm.leisure.dinnerplan.dto.ProposalSuggestionDTO> proposals = new ArrayList<>();
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

        for (String encoded : selectedProposals) {
            try {
                String json = new String(java.util.Base64.getDecoder().decode(encoded));
                proposals.add(mapper.readValue(json, it.ucdm.leisure.dinnerplan.dto.ProposalSuggestionDTO.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        model.addAttribute("selectedProposals", proposals);
        return "create_smart_event";
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/events/create-smart")
    public String createSmartEvent(@ModelAttribute it.ucdm.leisure.dinnerplan.dto.SmartEventRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        DinnerEvent event = dinnerService.createEventWithProposals(
                request.getTitle(),
                request.getDescription(),
                LocalDateTime.parse(request.getDeadline()),
                userDetails.getUsername(),
                request.getProposals());

        return "redirect:/events/" + event.getId();
    }

    @PostMapping("/events/{id}/delete")
    @PreAuthorize("hasRole('ORGANIZER')")
    public String deleteEvent(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        dinnerService.deleteEvent(id, userDetails.getUsername());
        return "redirect:/";
    }
}
