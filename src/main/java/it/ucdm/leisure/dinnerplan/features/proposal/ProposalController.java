package it.ucdm.leisure.dinnerplan.features.proposal;

import it.ucdm.leisure.dinnerplan.features.event.InteractionService;
import it.ucdm.leisure.dinnerplan.features.user.DietaryPreference;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    @PostMapping("/proposals/add")
    public String addGlobalProposal(@RequestParam String location, @RequestParam String address,
            @RequestParam String description, @RequestParam(required = false) List<String> dietaryPreferences,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        if (dietaryPreferences == null || dietaryPreferences.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Devi selezionare almeno una preferenza alimentare.");
            return "redirect:/";
        }

        Set<DietaryPreference> preferences = dietaryPreferences.stream()
                .map(DietaryPreference::valueOf)
                .collect(Collectors.toSet());

        proposalCatalogService.addGlobalProposal(location, address, description, preferences);
        return "redirect:/";
    }

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("/proposals/update-dietary")
    public String updateProposalDietary(@RequestParam String location, @RequestParam String address,
            @RequestParam(required = false) List<String> dietaryPreferences,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        if (dietaryPreferences == null || dietaryPreferences.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Devi selezionare almeno una preferenza alimentare.");
            return "redirect:/";
        }

        Set<DietaryPreference> preferences = dietaryPreferences.stream()
                .map(DietaryPreference::valueOf)
                .collect(Collectors.toSet());

        proposalCatalogService.updateGlobalProposalDietary(location, address, preferences);
        redirectAttributes.addFlashAttribute("successMessage", "Preferenze alimentari aggiornate con successo.");
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
