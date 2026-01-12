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

        if (event.getStatus() == DinnerEvent.EventStatus.DECIDED && event.getSelectedProposalDate() != null) {
            LocalDateTime start = event.getSelectedProposalDate().getDate();
            builder.append("DTSTART:").append(formatDate(start)).append("\n");
            builder.append("DTEND:").append(formatDate(start.plusHours(2))).append("\n");

            it.ucdm.leisure.dinnerplan.features.proposal.Proposal proposal = event.getSelectedProposalDate()
                    .getProposal();
            if (proposal != null) {
                if (proposal.getLocation() != null && !proposal.getLocation().isEmpty()) {
                    String location = proposal.getLocation();
                    if (proposal.getAddress() != null && !proposal.getAddress().isEmpty()) {
                        location += " (" + proposal.getAddress() + ")";
                    }
                    builder.append("LOCATION:").append(location).append("\n");
                }

                StringBuilder desc = new StringBuilder(event.getDescription() != null ? event.getDescription() : "");
                if (proposal.getDescription() != null && !proposal.getDescription().isEmpty()) {
                    if (desc.length() > 0)
                        desc.append("\\n\\n");
                    desc.append("Dettagli Proposta:\\n").append(proposal.getDescription());
                }
                builder.append("DESCRIPTION:").append(desc.toString()).append("\n");
            } else {
                builder.append("SUMMARY:").append(event.getTitle()).append("\n");
                builder.append("DESCRIPTION:").append(event.getDescription() != null ? event.getDescription() : "")
                        .append("\n");
            }
        } else {
            // Default to OPEN status or if no selection made
            LocalDateTime start = event.getDeadline() != null ? event.getDeadline() : LocalDateTime.now();
            builder.append("DTSTART:").append(formatDate(start)).append("\n");
            builder.append("DTEND:").append(formatDate(start.plusHours(2))).append("\n");
            builder.append("SUMMARY:").append(event.getTitle()).append("\n");
            builder.append("DESCRIPTION:").append(event.getDescription() != null ? event.getDescription() : "")
                    .append("\n");
        }

        builder.append("SUMMARY:").append(event.getTitle()).append("\n");
        builder.append("END:VEVENT\n");
        builder.append("END:VCALENDAR\n");

        return builder.toString();
    }

    private String formatDate(LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"));
    }
}
