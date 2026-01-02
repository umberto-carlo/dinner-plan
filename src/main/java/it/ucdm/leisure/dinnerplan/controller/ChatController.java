package it.ucdm.leisure.dinnerplan.controller;

import it.ucdm.leisure.dinnerplan.service.InteractionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ChatController {

    private final InteractionService interactionService;

    public ChatController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping("/events/{id}/chat/send")
    @ResponseBody
    public ResponseEntity<?> sendMessage(@PathVariable Long id, @RequestParam String content,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            interactionService.addMessage(id, userDetails.getUsername(), content);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
