package it.ucdm.leisure.dinnerplan.controller.api;

import it.ucdm.leisure.dinnerplan.model.DinnerEventMessage;
import it.ucdm.leisure.dinnerplan.model.Vote;
import it.ucdm.leisure.dinnerplan.service.InteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/interactions")
@Tag(name = "Interactions", description = "Operations for voting, rating, messages, and deciding events")
public class InteractionRestController {

    private final InteractionService interactionService;

    public InteractionRestController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping("/proposals/{proposalDateId}/vote")
    @Operation(summary = "Cast or remove a vote for a proposal date")
    public ResponseEntity<Void> castVote(@PathVariable Long proposalDateId,
            @AuthenticationPrincipal UserDetails userDetails) {
        interactionService.castVote(proposalDateId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/events/{eventId}/decide/{proposalDateId}")
    @Operation(summary = "Decide the final proposal for the event (Organizer only)")
    public ResponseEntity<Void> decideEvent(@PathVariable Long eventId, @PathVariable Long proposalDateId,
            @AuthenticationPrincipal UserDetails userDetails) {
        interactionService.selectProposal(eventId, proposalDateId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/events/{eventId}/proposals/{proposalId}/rate")
    @Operation(summary = "Rate a proposal (Like/Dislike)")
    public ResponseEntity<Void> rateProposal(@PathVariable Long eventId, @PathVariable Long proposalId,
            @RequestParam boolean isLiked,
            @AuthenticationPrincipal UserDetails userDetails) {
        interactionService.rateProposal(eventId, proposalId, userDetails.getUsername(), isLiked);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/events/{eventId}/messages")
    @Operation(summary = "Send a chat message to the event group")
    public ResponseEntity<Void> sendMessage(@PathVariable Long eventId, @RequestBody String content,
            @AuthenticationPrincipal UserDetails userDetails) {
        interactionService.addMessage(eventId, userDetails.getUsername(), content);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/events/{eventId}/messages")
    @Operation(summary = "Get chat messages for an event")
    public ResponseEntity<List<DinnerEventMessage>> getMessages(@PathVariable Long eventId) {
        return ResponseEntity.ok(interactionService.getEventMessages(eventId));
    }
}
