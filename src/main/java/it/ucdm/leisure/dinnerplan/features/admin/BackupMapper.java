package it.ucdm.leisure.dinnerplan.features.admin;

import it.ucdm.leisure.dinnerplan.features.proposal.Proposal;
import it.ucdm.leisure.dinnerplan.features.user.User;
import it.ucdm.leisure.dinnerplan.features.event.DinnerEventMessage;
import it.ucdm.leisure.dinnerplan.features.proposal.Vote;
import it.ucdm.leisure.dinnerplan.features.proposal.ProposalRating;

import it.ucdm.leisure.dinnerplan.features.proposal.ProposalDate;
import it.ucdm.leisure.dinnerplan.features.event.DinnerEvent;

import it.ucdm.leisure.dinnerplan.dto.backup.*;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BackupMapper {

    public UserBackupDTO toBackupDTO(User user) {
        if (user == null)
            return null;
        return UserBackupDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword()) // Encrypted
                .role(user.getRole())
                .build();
    }

    public DinnerEventBackupDTO toBackupDTO(DinnerEvent event) {
        if (event == null)
            return null;
        return DinnerEventBackupDTO.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .deadline(event.getDeadline())
                .organizerId(event.getOrganizer().getId())
                .status(event.getStatus())
                .selectedProposalDateId(
                        event.getSelectedProposalDate() != null ? event.getSelectedProposalDate().getId() : null)
                .participantIds(event.getParticipants().stream().map(User::getId).collect(Collectors.toList()))
                .build();
    }

    public ProposalBackupDTO toBackupDTO(Proposal proposal) {
        if (proposal == null)
            return null;
        return ProposalBackupDTO.builder()
                .id(proposal.getId())
                .location(proposal.getLocation())
                .address(proposal.getAddress())
                .description(proposal.getDescription())
                .dinnerEventIds(
                        proposal.getDinnerEvents().stream().map(DinnerEvent::getId).collect(Collectors.toList()))
                .build();
    }

    public ProposalDateBackupDTO toBackupDTO(ProposalDate pd) {
        if (pd == null)
            return null;
        return ProposalDateBackupDTO.builder()
                .id(pd.getId())
                .date(pd.getDate())
                .proposalId(pd.getProposal().getId())
                .dinnerEventId(pd.getDinnerEvent() != null ? pd.getDinnerEvent().getId() : null)
                .build();
    }

    public VoteBackupDTO toBackupDTO(Vote vote) {
        if (vote == null)
            return null;
        return VoteBackupDTO.builder()
                .id(vote.getId())
                .userId(vote.getUser().getId())
                .proposalDateId(vote.getProposalDate().getId())
                .build();
    }

    public DinnerEventMessageBackupDTO toBackupDTO(DinnerEventMessage msg) {
        if (msg == null)
            return null;
        return DinnerEventMessageBackupDTO.builder()
                .id(msg.getId())
                .eventId(msg.getEvent().getId())
                .senderId(msg.getSender().getId())
                .content(msg.getContent())
                .timestamp(msg.getTimestamp())
                .build();
    }

    public ProposalRatingBackupDTO toBackupDTO(ProposalRating rating) {
        if (rating == null)
            return null;
        return ProposalRatingBackupDTO.builder()
                .id(rating.getId())
                .userId(rating.getUser().getId())
                .proposalId(rating.getProposal().getId())
                .isLiked(rating.isLiked())
                .build();
    }

    public List<UserBackupDTO> mapUsers(List<User> users) {
        return users.stream().map(this::toBackupDTO).collect(Collectors.toList());
    }
}
