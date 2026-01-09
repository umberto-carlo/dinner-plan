package it.ucdm.leisure.dinnerplan.features.proposal;

import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import it.ucdm.leisure.dinnerplan.persistence.DinnerEventRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.ProposalRepositoryPort;

import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.features.user.Role;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ProposalServiceTest {

    @Mock
    private ProposalRepositoryPort proposalRepository; // Changed to Port
    @Mock
    private DinnerEventRepositoryPort dinnerEventRepository; // Changed to Port
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ProposalService proposalService;

    private User organizer;
    private DinnerEvent event;

    @BeforeEach
    void setUp() {
        org.springframework.transaction.support.TransactionSynchronizationManager.initSynchronization();

        organizer = new User();
        organizer.setId(1L);
        organizer.setUsername("organizer");
        organizer.setRole(Role.ORGANIZER);

        event = new DinnerEvent();
        event.setId(1L);
        event.setOrganizer(organizer);
        event.setStatus(DinnerEvent.EventStatus.OPEN);
        event.setDeadline(LocalDateTime.now().plusDays(5));
        event.setProposals(new ArrayList<>());
        event.setParticipants(new ArrayList<>());
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        org.springframework.transaction.support.TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    void addProposal_NewProposal_Success() {
        when(dinnerEventRepository.findById(1L)).thenReturn(Optional.of(event));
        // Global check logic removed, so no mock needed for findByLocation... in Port
        // if not used.
        // Wait, ProposalService checks event.getProposals().

        proposalService.addProposal(1L, List.of(LocalDateTime.now().plusDays(6)), "Loc", "Addr", "Desc");
        assertEquals(1, event.getProposals().size());
        assertEquals("Loc", event.getProposals().get(0).getLocation());
        // verify(dinnerEventRepository).save(event); // ProposalService removed
        // explicit save, relies on objectref??
        // Wait, check ProposalService. It ends with:
        // dinnerEventRepository.save(event);?
        // Let me check ProposalService content again.
        // Yes, line 135: dinnerEventRepository.save(event);
        verify(dinnerEventRepository).save(event);
    }

    // Removed reuse test as it's no longer implemented in Service

    @Test
    void addBatchProposals_Success() {
        when(dinnerEventRepository.findById(1L)).thenReturn(Optional.of(event));
        // when(proposalRepository.findByLocation...) no longer called.

        String json = "{\"location\":\"Loc1\",\"address\":\"Addr1\",\"description\":\"Desc1\",\"totalLikes\":0,\"totalDislikes\":0,\"usageCount\":0}";
        String encoded = Base64.getEncoder().encodeToString(json.getBytes());

        int count = proposalService.addBatchProposalsFromSuggestion(1L, List.of(LocalDateTime.now().plusDays(6)),
                List.of(encoded),
                "organizer");
        assertEquals(1, count);
        assertEquals(1, event.getProposals().size());
    }
}
