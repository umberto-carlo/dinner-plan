package it.ucdm.leisure.dinnerplan.features.admin;

import it.ucdm.leisure.dinnerplan.model.Proposal;
import it.ucdm.leisure.dinnerplan.model.User;
import it.ucdm.leisure.dinnerplan.model.DinnerEventMessage;
import it.ucdm.leisure.dinnerplan.model.Vote;
import it.ucdm.leisure.dinnerplan.model.ProposalRating;

import it.ucdm.leisure.dinnerplan.model.ProposalDate;
import it.ucdm.leisure.dinnerplan.model.DinnerEvent;

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
        DinnerEventBackupDTO dto = new DinnerEventBackupDTO();
        dto.setId(event.getId());
        dto.setTitle(event.getTitle());
        dto.setDescription(event.getDescription());
        dto.setDeadline(event.getDeadline());
        dto.setStatus(event.getStatus());
        dto.setOrganizer(event.getOrganizer().getUsername());
        dto.setParticipants(event.getParticipants().stream().map(User::getUsername).collect(Collectors.toList()));
        if (event.getProposals() != null) {
            dto.setProposalIds(event.getProposals().stream().map(Proposal::getId).collect(Collectors.toSet()));
        }
        return dto;
    }

    public ProposalBackupDTO toBackupDTO(Proposal proposal) {
        if (proposal == null)
            return null;
        return ProposalBackupDTO.builder()
                .id(proposal.getId())
                .location(proposal.getLocation())
                .address(proposal.getAddress())
                .description(proposal.getDescription())
                .dinnerEventIds(proposal.getDinnerEvent() != null ? List.of(proposal.getDinnerEvent().getId())
                        : java.util.Collections.emptyList())
                .build();
    }

    public ProposalDateBackupDTO toBackupDTO(ProposalDate pd) {
        if (pd == null)
            return null;
        return ProposalDateBackupDTO.builder()
                .id(pd.getId())
                .date(pd.getDate())
                .proposalId(pd.getProposal().getId())
                .dinnerEventId((pd.getProposal() != null && pd.getProposal().getDinnerEvent() != null)
                        ? pd.getProposal().getDinnerEvent().getId()
                        : null)
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
