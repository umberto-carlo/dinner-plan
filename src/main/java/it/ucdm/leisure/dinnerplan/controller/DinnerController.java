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
            model.addAttribute("user", user);
            model.addAttribute("isOrganizer", user.getRole() == Role.ORGANIZER);
        } else {
            // Default or empty for non-logged in?
            // Assuming login is required, otherwise redirect or empty.
            model.addAttribute("events", new ArrayList<>());
        }
        return "dashboard";
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

    @GetMapping("/events/{id}")
    public String eventDetails(@PathVariable Long id, Model model, @AuthenticationPrincipal UserDetails userDetails) {
        DinnerEvent event = dinnerService.getEventById(id);
        model.addAttribute("event", event);
        model.addAttribute("recentProposals", dinnerService.getProposalSuggestions());

        List<Proposal> sortedProposals = new ArrayList<>(event.getProposals());
        sortedProposals.sort(Comparator.comparingInt((Proposal p) -> p.getVotes().size())
                .reversed()
                .thenComparing(Proposal::getDateOption));
        model.addAttribute("sortedProposals", sortedProposals);

        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());

            // Access Control: Only Organizer or Participants (or Admin) can view
            boolean isParticipant = event.getParticipants().stream().anyMatch(p -> p.getId().equals(user.getId()));
            boolean isOrganizer = event.getOrganizer().getId().equals(user.getId());
            boolean isAdmin = user.getRole() == Role.ADMIN; // Assuming ADMIN has global access

            if (!isOrganizer && !isParticipant && !isAdmin) {
                return "redirect:/";
            }

            model.addAttribute("currentUser", user);
            model.addAttribute("isOrganizer", isOrganizer);

            // Get proposals voted by user
            var votes = dinnerService.getUserVotesForEvent(id, user.getId());
            var votedProposalIds = votes.stream().map(v -> v.getProposal().getId()).toList();
            model.addAttribute("votedProposalIds", votedProposalIds);

            if (event.getStatus() == DinnerEvent.EventStatus.DECIDED && event.getSelectedProposal() != null) {
                dinnerService.getUserRatingForProposal(event.getSelectedProposal().getId(), user.getId())
                        .ifPresent(rating -> model.addAttribute("userRating", rating));
            }

            if (model.getAttribute("isOrganizer") != null && (Boolean) model.getAttribute("isOrganizer")) {
                model.addAttribute("allUsers", userService.getAllUsers());
            }
        }
        return "event_details";
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
}
