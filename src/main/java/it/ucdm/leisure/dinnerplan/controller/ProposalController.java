package it.ucdm.leisure.dinnerplan.controller;

import it.ucdm.leisure.dinnerplan.service.ProposalService;
import it.ucdm.leisure.dinnerplan.service.InteractionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ProposalController {

    private final ProposalService proposalService;
    private final InteractionService interactionService;

    public ProposalController(ProposalService proposalService, InteractionService interactionService) {
        this.proposalService = proposalService;
        this.interactionService = interactionService;
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/events/{id}/add-proposal")
    public String addProposal(@PathVariable Long id, @RequestParam String dateOption, @RequestParam String location,
            @RequestParam String address, @RequestParam String description,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            LocalDateTime dt = LocalDateTime.parse(dateOption);
            proposalService.addProposal(id, dt, location, address, description);
            return "redirect:/events/" + id;
        } catch (Exception e) {
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
            proposalService.addGlobalProposal(location, address, description);
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
            proposalService.addProposalFromSuggestion(eventId, dt, location, address, description,
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
                    dts.add(null);
                }
            }

            int count = proposalService.addBatchProposalsFromSuggestion(eventId, dts, selectedProposals,
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
        interactionService.castVote(id, userDetails.getUsername());
        return "redirect:/events/" + eventId;
    }

    @PostMapping("/events/{id}/select-proposal")
    public String selectProposal(@PathVariable Long id, @RequestParam Long proposalId,
            @AuthenticationPrincipal UserDetails userDetails) {
        interactionService.selectProposal(id, proposalId, userDetails.getUsername());
        return "redirect:/events/" + id;
    }

    @PostMapping("/events/{id}/rate")
    public String rateProposal(@PathVariable Long id, @RequestParam Long proposalId, @RequestParam boolean isLiked,
            @AuthenticationPrincipal UserDetails userDetails) {
        interactionService.rateProposal(id, proposalId, userDetails.getUsername(), isLiked);
        return "redirect:/events/" + id;
    }
}
