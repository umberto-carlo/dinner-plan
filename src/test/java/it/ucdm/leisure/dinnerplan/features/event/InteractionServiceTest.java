package it.ucdm.leisure.dinnerplan.features.event;

import it.ucdm.leisure.dinnerplan.features.proposal.Proposal;
import it.ucdm.leisure.dinnerplan.features.user.User;

import it.ucdm.leisure.dinnerplan.features.proposal.ProposalRating;
import it.ucdm.leisure.dinnerplan.features.user.Role;
import it.ucdm.leisure.dinnerplan.features.proposal.Vote;
import it.ucdm.leisure.dinnerplan.features.proposal.ProposalDate;
import it.ucdm.leisure.dinnerplan.features.proposal.ProposalRepository;
import it.ucdm.leisure.dinnerplan.features.proposal.ProposalRatingRepository;
import it.ucdm.leisure.dinnerplan.features.proposal.VoteRepository;
import it.ucdm.leisure.dinnerplan.features.proposal.ProposalDateRepository;

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
    private VoteRepository voteRepository;
    @Mock
    private ProposalRepository proposalRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private DinnerEventRepository dinnerEventRepository;
    @Mock
    private ProposalRatingRepository proposalRatingRepository;
    @Mock
    private DinnerEventMessageRepository dinnerEventMessageRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private ProposalDateRepository proposalDateRepository;

    @InjectMocks
    private InteractionService interactionService;

    private User participant;
    private DinnerEvent event;
    private Proposal proposal;

    @BeforeEach
    void setUp() {
        org.springframework.transaction.support.TransactionSynchronizationManager.initSynchronization();

        participant = User.builder().id(2L).username("participant").role(Role.PARTICIPANT).build();
        event = DinnerEvent.builder().id(1L).deadline(LocalDateTime.now().plusDays(1))
                .participants(List.of(participant)).organizer(User.builder().username("org").build()).build();
        proposal = Proposal.builder().id(1L).dinnerEvents(new ArrayList<>(List.of(event))).build();
        ProposalDate pd = ProposalDate.builder().id(10L).date(LocalDateTime.now()).proposal(proposal).dinnerEvent(event)
                .build();
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

        when(proposalDateRepository.findById(10L)).thenReturn(Optional.of(pd));
        when(voteRepository.findByUserAndProposalDate(participant, pd)).thenReturn(Optional.empty());

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
        verify(messagingTemplate).convertAndSend(anyString(), any(Object.class));
    }
}
