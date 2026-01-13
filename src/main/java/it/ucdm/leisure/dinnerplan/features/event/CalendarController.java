package it.ucdm.leisure.dinnerplan.features.event;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import it.ucdm.leisure.dinnerplan.service.CalendarService;

import java.util.List;

@RestController
public class CalendarController {

    private final DinnerEventService dinnerEventService;
    private final CalendarService calendarService;

    public CalendarController(DinnerEventService dinnerEventService, CalendarService calendarService) {
        this.dinnerEventService = dinnerEventService;
        this.calendarService = calendarService;
    }

    @GetMapping("/events/{id}/ics")
    public ResponseEntity<Resource> downloadIcs(@PathVariable Long id) {
        DinnerEvent event = dinnerEventService.getEventById(id);
        String icsContent = calendarService.generateIcsContent(event);
        ByteArrayResource resource = new ByteArrayResource(icsContent.getBytes());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"event-" + id + ".ics\"")
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(resource);
    }

    @GetMapping("/api/calendar/export")
    public ResponseEntity<Resource> exportAll(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        List<DinnerEvent> events = dinnerEventService.getEventsForUser(userDetails.getUsername());
        String icsContent = calendarService.generateIcsContent(events);
        ByteArrayResource resource = new ByteArrayResource(icsContent.getBytes());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"all-events.ics\"")
                .contentType(MediaType.parseMediaType("text/calendar"))
                .body(resource);
    }
}
