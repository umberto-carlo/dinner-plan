package it.ucdm.leisure.dinnerplan.controller.api;

import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import it.ucdm.leisure.dinnerplan.dto.ProposalSuggestionDTO;
import it.ucdm.leisure.dinnerplan.service.DinnerEventService;
import it.ucdm.leisure.dinnerplan.service.ProposalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Dinner Events", description = "Operations related to dinner events")
public class DinnerRestController {

    private final DinnerEventService dinnerEventService;
    private final ProposalService proposalService;

    public DinnerRestController(DinnerEventService dinnerEventService, ProposalService proposalService) {
        this.dinnerEventService = dinnerEventService;
        this.proposalService = proposalService;
    }

    @GetMapping
    @Operation(summary = "Get all events for the current user")
    public ResponseEntity<List<DinnerEvent>> getEvents(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dinnerEventService.getEventsForUser(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific event by ID")
    public ResponseEntity<DinnerEvent> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(dinnerEventService.getEventById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new dinner event")
    public ResponseEntity<DinnerEvent> createEvent(@RequestParam String title,
            @RequestParam String description,
            @RequestParam LocalDateTime deadline,
            @RequestBody(required = false) List<Long> participantIds,
            @AuthenticationPrincipal UserDetails userDetails) {
        DinnerEvent createdEvent = dinnerEventService.createEvent(title, description, deadline,
                userDetails.getUsername(), participantIds);
        return ResponseEntity.ok(createdEvent);
    }

    @PutMapping("/{id}/participants")
    @Operation(summary = "Update participants for an event")
    public ResponseEntity<Void> updateParticipants(@PathVariable Long id,
            @RequestBody List<Long> participantIds,
            @AuthenticationPrincipal UserDetails userDetails) {
        dinnerEventService.updateParticipants(id, participantIds, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an event")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        dinnerEventService.deleteEvent(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/proposals/suggestions")
    @Operation(summary = "Get proposal suggestions")
    public ResponseEntity<List<ProposalSuggestionDTO>> getProposalSuggestions() {
        return ResponseEntity.ok(proposalService.getProposalSuggestions());
    }
}
