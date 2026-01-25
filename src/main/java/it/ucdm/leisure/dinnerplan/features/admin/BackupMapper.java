package it.ucdm.leisure.dinnerplan.features.admin;

import it.ucdm.leisure.dinnerplan.dto.backup.*;
import it.ucdm.leisure.dinnerplan.features.event.DinnerEvent;
import it.ucdm.leisure.dinnerplan.features.event.DinnerEventMessage;
import it.ucdm.leisure.dinnerplan.features.proposal.Proposal;
import it.ucdm.leisure.dinnerplan.features.proposal.ProposalDate;
import it.ucdm.leisure.dinnerplan.features.proposal.ProposalRating;
import it.ucdm.leisure.dinnerplan.features.proposal.Vote;
import it.ucdm.leisure.dinnerplan.features.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BackupMapper {

    UserBackupDTO toBackupDTO(User user);

    @Mapping(source = "organizer.id", target = "organizerId")
    @Mapping(source = "selectedProposalDate.id", target = "selectedProposalDateId")
    @Mapping(source = "participants", target = "participantIds", qualifiedByName = "mapUsersToIds")
    DinnerEventBackupDTO toBackupDTO(DinnerEvent event);

    @Mapping(source = "dinnerEvents", target = "dinnerEventIds", qualifiedByName = "mapEventsToIds")
    ProposalBackupDTO toBackupDTO(Proposal proposal);

    @Mapping(source = "proposal.id", target = "proposalId")
    @Mapping(source = "dinnerEvent.id", target = "dinnerEventId")
    ProposalDateBackupDTO toBackupDTO(ProposalDate pd);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "proposalDate.id", target = "proposalDateId")
    VoteBackupDTO toBackupDTO(Vote vote);

    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "sender.id", target = "senderId")
    DinnerEventMessageBackupDTO toBackupDTO(DinnerEventMessage msg);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "proposal.id", target = "proposalId")
    ProposalRatingBackupDTO toBackupDTO(ProposalRating rating);

    @Named("mapUsersToIds")
    default List<Long> mapUsersToIds(List<User> users) {
        if (users == null) return null;
        return users.stream().map(User::getId).collect(Collectors.toList());
    }

    @Named("mapEventsToIds")
    default List<Long> mapEventsToIds(List<DinnerEvent> events) {
        if (events == null) return null;
        return events.stream().map(DinnerEvent::getId).collect(Collectors.toList());
    }
}
