package it.ucdm.leisure.dinnerplan.service;

import it.ucdm.leisure.dinnerplan.features.event.DinnerEvent;
import it.ucdm.leisure.dinnerplan.features.proposal.Proposal;
import it.ucdm.leisure.dinnerplan.features.proposal.ProposalDate;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CalendarServiceTest {

    private final CalendarService calendarService = new CalendarService();

    @Test
    public void testGenerateIcsContentOpenEvent() {
        LocalDateTime deadline = LocalDateTime.of(2026, 1, 20, 19, 0);
        DinnerEvent event = DinnerEvent.builder()
                .id(1L)
                .title("Cena di prova")
                .description("Descrizione cena")
                .status(DinnerEvent.EventStatus.OPEN)
                .deadline(deadline)
                .build();

        String ics = calendarService.generateIcsContent(event);

        assertTrue(ics.contains("SUMMARY:Cena di prova"));
        assertTrue(ics.contains("DESCRIPTION:Descrizione cena"));
        assertTrue(ics.contains("DTSTART:20260120T190000"));
    }

    @Test
    public void testGenerateIcsContentDecidedEvent() {
        LocalDateTime eventDate = LocalDateTime.of(2026, 1, 25, 20, 30);

        Proposal proposal = Proposal.builder()
                .location("Ristorante Bella Napoli")
                .address("Via Roma 1")
                .description("Ottima pizza")
                .build();

        ProposalDate selectedDate = ProposalDate.builder()
                .date(eventDate)
                .proposal(proposal)
                .build();

        DinnerEvent event = DinnerEvent.builder()
                .id(2L)
                .title("Cena Decisa")
                .description("Descrizione cena")
                .status(DinnerEvent.EventStatus.DECIDED)
                .selectedProposalDate(selectedDate)
                .build();

        String ics = calendarService.generateIcsContent(event);

        assertTrue(ics.contains("SUMMARY:Cena Decisa"));
        assertTrue(ics.contains("LOCATION:Ristorante Bella Napoli (Via Roma 1)"));
        assertTrue(ics.contains("DTSTART:20260125T203000"));
        assertTrue(ics.contains("DESCRIPTION:Descrizione cena\\n\\nDettagli Proposta:\\nOttima pizza"));
    }
}
