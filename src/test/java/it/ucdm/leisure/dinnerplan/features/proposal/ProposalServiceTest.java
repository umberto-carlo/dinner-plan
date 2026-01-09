package it.ucdm.leisure.dinnerplan.features.proposal;

import it.ucdm.leisure.dinnerplan.features.event.DinnerEvent;
import it.ucdm.leisure.dinnerplan.features.event.DinnerEventRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.ucdm.leisure.dinnerplan.features.user.*;

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
    private ProposalRepository proposalRepository;
    @Mock
    private DinnerEventRepository dinnerEventRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ProposalService proposalService;

    private User organizer;
    private DinnerEvent event;

    @BeforeEach
    void setUp() {
        org.springframework.transaction.support.TransactionSynchronizationManager.initSynchronization();

        organizer = User.builder().id(1L).username("organizer").role(Role.ORGANIZER).build();
        event = DinnerEvent.builder().id(1L).organizer(organizer).status(DinnerEvent.EventStatus.OPEN)
                .deadline(LocalDateTime.now())
                .proposals(new ArrayList<>()).build();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        org.springframework.transaction.support.TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    void addProposal_NewProposal_Success() {
        when(dinnerEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(proposalRepository.findByLocationIgnoreCaseAndAddressIgnoreCase(anyString(), anyString()))
                .thenReturn(Optional.empty());

        proposalService.addProposal(1L, List.of(LocalDateTime.now().plusDays(1)), "Loc", "Addr", "Desc");
        assertEquals(1, event.getProposals().size());
        verify(dinnerEventRepository).save(event);
    }

    @Test
    void addProposal_ExistingProposal_Reuse() {
        when(dinnerEventRepository.findById(1L)).thenReturn(Optional.of(event));
        Proposal existing = Proposal.builder().id(99L).dinnerEvents(new ArrayList<>()).location("Loc").address("Addr")
                .build();
        when(proposalRepository.findByLocationIgnoreCaseAndAddressIgnoreCase("Loc", "Addr"))
                .thenReturn(Optional.of(existing));

        proposalService.addProposal(1L, List.of(LocalDateTime.now().plusDays(1)), "Loc", "Addr", "Desc");

        assertEquals(1, event.getProposals().size());
        assertEquals(99L, event.getProposals().get(0).getId());
        assertTrue(existing.getDinnerEvents().contains(event));
        verify(dinnerEventRepository).save(event);
    }

    @Test
    void addBatchProposals_Success() {
        when(dinnerEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(proposalRepository.findByLocationIgnoreCaseAndAddressIgnoreCase(anyString(), anyString()))
                .thenReturn(Optional.empty());
        String json = "{\"location\":\"Loc1\",\"address\":\"Addr1\",\"description\":\"Desc1\",\"totalLikes\":0,\"totalDislikes\":0,\"usageCount\":0}";
        String encoded = Base64.getEncoder().encodeToString(json.getBytes());

        int count = proposalService.addBatchProposalsFromSuggestion(1L, List.of(LocalDateTime.now().plusDays(1)),
                List.of(encoded),
                "organizer");
        assertEquals(1, count);
    }
}
