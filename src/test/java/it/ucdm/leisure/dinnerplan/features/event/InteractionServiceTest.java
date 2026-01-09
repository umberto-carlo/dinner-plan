package it.ucdm.leisure.dinnerplan.features.event;

import it.ucdm.leisure.dinnerplan.model.Proposal;
import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.model.ProposalRating;
import it.ucdm.leisure.dinnerplan.features.user.Role;
import it.ucdm.leisure.dinnerplan.model.Vote;
import it.ucdm.leisure.dinnerplan.model.ProposalDate;
import it.ucdm.leisure.dinnerplan.model.DinnerEvent;
import it.ucdm.leisure.dinnerplan.model.DinnerEventMessage;

import it.ucdm.leisure.dinnerplan.persistence.ProposalRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.ProposalRatingRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.VoteRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.UserRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.DinnerEventRepositoryPort;
import it.ucdm.leisure.dinnerplan.persistence.DinnerEventMessageRepositoryPort;

import static org.mockito.ArgumentMatchers.any;
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
class InteractionServiceTest {

    @Mock
    private VoteRepositoryPort voteRepository;
    @Mock
    private ProposalRepositoryPort proposalRepository;
    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private DinnerEventRepositoryPort dinnerEventRepository;
    @Mock
    private ProposalRatingRepositoryPort proposalRatingRepository;
    @Mock
    private DinnerEventMessageRepositoryPort dinnerEventMessageRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private InteractionService interactionService;

    private User participant;
    private DinnerEvent event;
    private Proposal proposal;

    @BeforeEach
    void setUp() {
        org.springframework.transaction.support.TransactionSynchronizationManager.initSynchronization();

        participant = new User(2L, "participant", "pass", Role.PARTICIPANT);
        event = new DinnerEvent();
        event.setId(1L);
        event.setDeadline(LocalDateTime.now().plusDays(1));
        event.setParticipants(new ArrayList<>(List.of(participant)));
        User org = new User();
        org.setUsername("org");
        event.setOrganizer(org);

        proposal = new Proposal();
        proposal.setId(1L);
        proposal.setDinnerEvent(event);

        ProposalDate pd = new ProposalDate();
        pd.setId(10L);
        pd.setDate(LocalDateTime.now());
        pd.setProposal(proposal);
        // pd.setDinnerEvent(event); // Not available or needed via proposal

        proposal.setDates(List.of(pd));
        event.setProposals(new ArrayList<>(List.of(proposal)));
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        org.springframework.transaction.support.TransactionSynchronizationManager.clearSynchronization();
    }

    @Test
    void castVote_Success() {
        when(userRepository.findByUsername("participant")).thenReturn(Optional.of(participant));

        ProposalDate pd = proposal.getDates().get(0);

        when(proposalRepository.findAll()).thenReturn(List.of(proposal)); // InteractionService might verify event
                                                                          // membership
        // Wait, castVote implementation might need Proposal Repository?
        // Let's assume implementation uses findByUserAndProposalDate from VoteRepo
        when(voteRepository.findByUserAndProposalDate(participant, pd)).thenReturn(Optional.empty());
        when(dinnerEventRepository.findById(1L)).thenReturn(Optional.of(event));

        interactionService.castVote(10L, "participant");
        verify(voteRepository).save(any(Vote.class));
    }

    @Test
    void rateProposal_Success() {
        event.setStatus(DinnerEvent.EventStatus.DECIDED);
        event.setSelectedProposalDate(proposal.getDates().get(0));
        when(dinnerEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(proposalRepository.findById(1L)).thenReturn(Optional.of(proposal));
        when(userRepository.findByUsername("participant")).thenReturn(Optional.of(participant));

        interactionService.rateProposal(1L, 1L, "participant", true);
        verify(proposalRatingRepository).save(any(ProposalRating.class));
    }

    @Test
    void addMessage_Success() {
        when(dinnerEventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findByUsername("participant")).thenReturn(Optional.of(participant));
        when(dinnerEventMessageRepository.save(any(DinnerEventMessage.class))).thenAnswer(i -> {
            DinnerEventMessage m = i.getArgument(0);
            m.setId(1L);
            return m;
        });

        interactionService.addMessage(1L, "participant", "Hello");
        verify(messagingTemplate).convertAndSend(anyString(), (Object) any());
    }
}
