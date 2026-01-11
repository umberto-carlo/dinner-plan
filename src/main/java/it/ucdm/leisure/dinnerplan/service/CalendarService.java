package it.ucdm.leisure.dinnerplan.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import it.ucdm.leisure.dinnerplan.features.event.DinnerEvent;

@Service
public class CalendarService {

    public String generateIcsContent(DinnerEvent event) {
        StringBuilder builder = new StringBuilder();
        builder.append("BEGIN:VCALENDAR\n");
        builder.append("VERSION:2.0\n");
        builder.append("PRODID:-//DinnerPlan//NONSGML v1.0//EN\n");
        builder.append("BEGIN:VEVENT\n");
        builder.append("UID:").append(event.getId()).append("@dinnerplan.com\n");
        builder.append("DTSTAMP:").append(formatDate(LocalDateTime.now())).append("\n");
        builder.append("DTSTART:").append(formatDate(event.getDeadline())).append("\n"); // Using deadline as start for
                                                                                         // now
        // Assuming 2 hour duration for simplicity or maybe we can improve this logic
        // later
        builder.append("DTEND:").append(formatDate(event.getDeadline().plusHours(2))).append("\n");
        builder.append("SUMMARY:").append(event.getTitle()).append("\n");
        builder.append("DESCRIPTION:").append(event.getDescription()).append("\n");
        builder.append("END:VEVENT\n");
        builder.append("END:VCALENDAR\n");

        return builder.toString();
    }

    private String formatDate(LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
    }
}
