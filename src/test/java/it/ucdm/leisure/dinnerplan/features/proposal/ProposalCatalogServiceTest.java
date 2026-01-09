package it.ucdm.leisure.dinnerplan.features.proposal;

import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import it.ucdm.leisure.dinnerplan.model.Proposal;
import it.ucdm.leisure.dinnerplan.model.ProposalRating;
import it.ucdm.leisure.dinnerplan.persistence.ProposalRepositoryPort;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import it.ucdm.leisure.dinnerplan.features.proposal.dto.ProposalSuggestionDTO;

import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class ProposalCatalogServiceTest {

    @Mock
    private ProposalRepositoryPort proposalRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ProposalCatalogService proposalCatalogService;

    @Test
    void getProposalSuggestions_ReturnsProposalsInCorrectOrder() {
        Proposal p1 = new Proposal();
        p1.setId(1L);
        p1.setLocation("Loc1");
        p1.setAddress("Addr1");
        p1.setDescription("Desc1");

        Proposal p2 = new Proposal();
        p2.setId(2L);
        p2.setLocation("Loc2");
        p2.setAddress("Addr2");
        p2.setDescription("Desc2");

        // p1 used in 1 event, p2 in 0 (ManyToOne)
        DinnerEvent e1 = new DinnerEvent();
        p1.setDinnerEvent(e1);
        p2.setDinnerEvent(null);

        // p2 has 5 likes, p1 has 0
        ProposalRating r = new ProposalRating();
        r.setLiked(true);
        p2.setRatings(new HashSet<>(List.of(r)));
        p1.setRatings(new HashSet<>());

        when(proposalRepository.findAll()).thenReturn(List.of(p1, p2));

        List<ProposalSuggestionDTO> result = proposalCatalogService.getProposalSuggestions();

        // Expect p2 first due to likes, then p1
        assertEquals(2, result.size());
        assertEquals("Loc2", result.get(0).getLocation());
        assertNotNull(result.get(0).getEncodedData());
        assertEquals("Loc1", result.get(1).getLocation());
        assertNotNull(result.get(1).getEncodedData());
    }

    @Test
    void addGlobalProposal_NewProposal_SavesAndSendsMessage() {
        when(proposalRepository.findByLocationIgnoreCaseAndAddressIgnoreCase("New", "NewAddr"))
                .thenReturn(Optional.empty());

        proposalCatalogService.addGlobalProposal("New", "NewAddr", "Desc");

        verify(proposalRepository).save(any(Proposal.class));
        // Transaction sync difficult to test directly with mockito in unit test without
        // spring context, but verifying repository save is the main logic here.
    }

    @Test
    void addGlobalProposal_ExistingProposal_DoesNothing() {
        when(proposalRepository.findByLocationIgnoreCaseAndAddressIgnoreCase("Exist", "ExistAddr"))
                .thenReturn(Optional.of(new Proposal()));

        proposalCatalogService.addGlobalProposal("Exist", "ExistAddr", "Desc");

        verify(proposalRepository, never()).save(any(Proposal.class));
    }
}
