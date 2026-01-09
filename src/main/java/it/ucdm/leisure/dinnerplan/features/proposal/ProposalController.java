package it.ucdm.leisure.dinnerplan.features.proposal;

import it.ucdm.leisure.dinnerplan.features.event.InteractionService;
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
    private final ProposalCatalogService proposalCatalogService;
    private final InteractionService interactionService;

    public ProposalController(ProposalService proposalService, ProposalCatalogService proposalCatalogService,
            InteractionService interactionService) {
        this.proposalService = proposalService;
        this.proposalCatalogService = proposalCatalogService;
        this.interactionService = interactionService;
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/events/{id}/add-proposal")
    public String addProposal(@PathVariable Long id, @RequestParam("dateOption") List<String> dateOptions,
            @RequestParam String location,
            @RequestParam String address, @RequestParam String description) {
        List<LocalDateTime> dts = new ArrayList<>();
        for (String d : dateOptions) {
            if (!d.isBlank()) {
                dts.add(LocalDateTime.parse(d));
            }
        }
        proposalService.addProposal(id, dts, location, address, description);
        return "redirect:/events/" + id;
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/proposals/add")
    public String addGlobalProposal(@RequestParam String location, @RequestParam String address,
            @RequestParam String description) {
        proposalCatalogService.addGlobalProposal(location, address, description);
        return "redirect:/";
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/proposals/add-to-event")
    public String addSuggestionToEvent(@RequestParam Long eventId, @RequestParam String location,
            @RequestParam String address, @RequestParam String description, @RequestParam String dateOption,
            @AuthenticationPrincipal UserDetails userDetails,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        LocalDateTime dt = LocalDateTime.parse(dateOption);
        proposalService.addProposalFromSuggestion(eventId, List.of(dt), location, address, description,
                userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMessage", "Proposta aggiunta all'evento con successo.");
        return "redirect:/";
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/proposals/add-batch-to-event")
    public String addBatchSuggestionToEvent(@RequestParam Long eventId,
            @RequestParam("selectedProposals") List<String> selectedProposals,
            @RequestParam("dateOptions") List<String> dateOptions,
            @AuthenticationPrincipal UserDetails userDetails,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
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
