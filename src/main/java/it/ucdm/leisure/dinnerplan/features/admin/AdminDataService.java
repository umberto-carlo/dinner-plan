package it.ucdm.leisure.dinnerplan.features.admin;

import it.ucdm.leisure.dinnerplan.features.proposal.Proposal;
import it.ucdm.leisure.dinnerplan.features.user.User;
import it.ucdm.leisure.dinnerplan.features.event.DinnerEventMessage;
import it.ucdm.leisure.dinnerplan.features.proposal.Vote;
import it.ucdm.leisure.dinnerplan.features.proposal.ProposalRating;
import it.ucdm.leisure.dinnerplan.features.proposal.ProposalDate;
import it.ucdm.leisure.dinnerplan.features.event.DinnerEvent;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import it.ucdm.leisure.dinnerplan.dto.backup.*;
import it.ucdm.leisure.dinnerplan.features.user.*;
import it.ucdm.leisure.dinnerplan.features.event.*;
import it.ucdm.leisure.dinnerplan.features.proposal.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class AdminDataService {

    private final UserRepository userRepository;
    private final DinnerEventRepository dinnerEventRepository;
    private final ProposalRepository proposalRepository;
    private final ProposalDateRepository proposalDateRepository;
    private final ProposalRatingRepository proposalRatingRepository;
    private final VoteRepository voteRepository;
    private final DinnerEventMessageRepository messageRepository;
    private final BackupMapper backupMapper;
    private final ObjectMapper objectMapper;

    public AdminDataService(UserRepository userRepository,
            DinnerEventRepository dinnerEventRepository,
            ProposalRepository proposalRepository,
            ProposalDateRepository proposalDateRepository,
            ProposalRatingRepository proposalRatingRepository,
            VoteRepository voteRepository,
            DinnerEventMessageRepository messageRepository,
            BackupMapper backupMapper) {
        this.userRepository = userRepository;
        this.dinnerEventRepository = dinnerEventRepository;
        this.proposalRepository = proposalRepository;
        this.proposalDateRepository = proposalDateRepository;
        this.proposalRatingRepository = proposalRatingRepository;
        this.voteRepository = voteRepository;
        this.messageRepository = messageRepository;
        this.backupMapper = backupMapper;
        this.objectMapper = JsonMapper.builder()
                .disable(tools.jackson.core.StreamWriteFeature.AUTO_CLOSE_TARGET)
                .build();
    }

    @Transactional(readOnly = true)
    public byte[] exportData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {

            writeEntry(zos, "users.json",
                    userRepository.findAll().stream().map(backupMapper::toBackupDTO).collect(Collectors.toList()));
            writeEntry(zos, "events.json",
                    dinnerEventRepository.findAll().stream().map(backupMapper::toBackupDTO)
                            .collect(Collectors.toList()));
            writeEntry(zos, "proposals.json",
                    proposalRepository.findAll().stream().map(backupMapper::toBackupDTO).collect(Collectors.toList()));
            writeEntry(zos, "proposal_dates.json",
                    proposalDateRepository.findAll().stream().map(backupMapper::toBackupDTO)
                            .collect(Collectors.toList()));
            writeEntry(zos, "ratings.json",
                    proposalRatingRepository.findAll().stream().map(backupMapper::toBackupDTO)
                            .collect(Collectors.toList()));
            writeEntry(zos, "votes.json",
                    voteRepository.findAll().stream().map(backupMapper::toBackupDTO).collect(Collectors.toList()));
            writeEntry(zos, "messages.json",
                    messageRepository.findAll().stream().map(backupMapper::toBackupDTO).collect(Collectors.toList()));
        }
        return baos.toByteArray();
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public void importData(InputStream inputStream) throws IOException {
        Map<String, List<?>> dataMap = new HashMap<>();

        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                InputStream nonClosingZis = new java.io.FilterInputStream(zis) {
                    @Override
                    public void close() throws IOException {
                        // Do nothing to keep ZipInputStream open for next entry
                    }
                };

                if (name.equals("users.json")) {
                    dataMap.put(name, objectMapper.readValue(nonClosingZis, new TypeReference<List<UserBackupDTO>>() {
                    }));
                } else if (name.equals("events.json")) {
                    dataMap.put(name,
                            objectMapper.readValue(nonClosingZis, new TypeReference<List<DinnerEventBackupDTO>>() {
                            }));
                } else if (name.equals("proposals.json")) {
                    dataMap.put(name,
                            objectMapper.readValue(nonClosingZis, new TypeReference<List<ProposalBackupDTO>>() {
                            }));
                } else if (name.equals("proposal_dates.json")) {
                    dataMap.put(name,
                            objectMapper.readValue(nonClosingZis, new TypeReference<List<ProposalDateBackupDTO>>() {
                            }));
                } else if (name.equals("ratings.json")) {
                    dataMap.put(name,
                            objectMapper.readValue(nonClosingZis, new TypeReference<List<ProposalRatingBackupDTO>>() {
                            }));
                } else if (name.equals("votes.json")) {
                    dataMap.put(name, objectMapper.readValue(nonClosingZis, new TypeReference<List<VoteBackupDTO>>() {
                    }));
                } else if (name.equals("messages.json")) {
                    dataMap.put(name,
                            objectMapper.readValue(nonClosingZis,
                                    new TypeReference<List<DinnerEventMessageBackupDTO>>() {
                                    }));
                }
            }
        }

        // DELETE ALL DATA
        voteRepository.deleteAll();
        proposalRatingRepository.deleteAll();
        messageRepository.deleteAll();
        // Clear relationships first
        List<DinnerEvent> allEvents = dinnerEventRepository.findAll();
        for (DinnerEvent event : allEvents) {
            event.setSelectedProposalDate(null);
            event.getParticipants().clear(); // Clear many-to-many users
            // NEW: Clear many-to-many proposals
            event.getProposals().clear();
        }
        dinnerEventRepository.saveAll(allEvents);

        proposalDateRepository.deleteAll();
        proposalRepository.deleteAll();
        dinnerEventRepository.deleteAll();
        userRepository.deleteAll();

        // FLUSH to ensure deletes are executed before inserts
        voteRepository.flush();
        proposalRatingRepository.flush();
        messageRepository.flush();
        proposalDateRepository.flush();
        proposalRepository.flush();
        dinnerEventRepository.flush();
        userRepository.flush();

        // RESTORE DATA
        // Phase 1: Users
        Map<Long, User> userMap = new HashMap<>(); // Old ID -> New Entity
        List<UserBackupDTO> userDTOs = (List<UserBackupDTO>) dataMap.getOrDefault("users.json",
                Collections.emptyList());
        for (UserBackupDTO dto : userDTOs) {
            User user = new User();
            user.setUsername(dto.getUsername());
            user.setPassword(dto.getPassword());
            user.setRole(dto.getRole());
            user = userRepository.save(user);
            userMap.put(dto.getId(), user);
        }

        // Phase 2: Events (Basic)
        Map<Long, DinnerEvent> eventMap = new HashMap<>();
        List<DinnerEventBackupDTO> eventDTOs = (List<DinnerEventBackupDTO>) dataMap.getOrDefault("events.json",
                Collections.emptyList());
        for (DinnerEventBackupDTO dto : eventDTOs) {
            DinnerEvent event = new DinnerEvent();
            event.setTitle(dto.getTitle());
            event.setDescription(dto.getDescription());
            event.setDeadline(dto.getDeadline());
            event.setStatus(dto.getStatus());
            event.setOrganizer(userMap.get(dto.getOrganizerId()));

            // Participants
            List<User> participants = new ArrayList<>();
            if (dto.getParticipantIds() != null) {
                for (Long pId : dto.getParticipantIds()) {
                    if (userMap.containsKey(pId)) {
                        participants.add(userMap.get(pId));
                    }
                }
            }
            event.setParticipants(participants);

            // Proposals (ManyToMany) - Initialize empty, specific links come from Proposal
            // side or reverse update
            event.setProposals(new ArrayList<>());

            event = dinnerEventRepository.save(event);
            eventMap.put(dto.getId(), event);
        }

        // Phase 3: Proposals
        Map<Long, Proposal> proposalMap = new HashMap<>();
        List<ProposalBackupDTO> proposalDTOs = (List<ProposalBackupDTO>) dataMap.getOrDefault("proposals.json",
                Collections.emptyList());
        for (ProposalBackupDTO dto : proposalDTOs) {
            Proposal proposal = new Proposal();
            proposal.setLocation(dto.getLocation());
            proposal.setAddress(dto.getAddress());
            proposal.setDescription(dto.getDescription());

            // Link to Events (ManyToMany)
            List<DinnerEvent> events = new ArrayList<>();
            if (dto.getDinnerEventIds() != null) {
                for (Long eventId : dto.getDinnerEventIds()) {
                    DinnerEvent ev = eventMap.get(eventId);
                    if (ev != null) {
                        events.add(ev);
                        // Also update the other side for consistency in validation, although DB handles
                        // it via JoinTable if persisted correctly
                        // But since Proposal is the inverse side (mappedBy=proposals in Proposal),
                        // DinnerEvent owns it.
                        // Wait, DinnerEvent owns it (@JoinTable on DinnerEvent). So we should add
                        // proposal to the event's collection.
                    }
                }
            }
            // Logic adjust: Proposal is mappedBy="proposals" (inverse). DinnerEvent is
            // owner.
            // We save Proposal. Then we update DinnerEvents to include this proposal.

            proposal = proposalRepository.save(proposal);
            proposalMap.put(dto.getId(), proposal);

            // Update Owner side (DinnerEvent)
            for (DinnerEvent ev : events) {
                // Determine if we need to re-fetch to avoid stale state?
                // We have references to managed entities in eventMap, but let's be safe.
                ev.getProposals().add(proposal);
                dinnerEventRepository.save(ev); // Persist the relationship in JoinTable
            }
        }

        // Phase 4: Proposal Dates
        Map<Long, ProposalDate> dateMap = new HashMap<>();
        List<ProposalDateBackupDTO> dateDTOs = (List<ProposalDateBackupDTO>) dataMap.getOrDefault("proposal_dates.json",
                Collections.emptyList());
        for (ProposalDateBackupDTO dto : dateDTOs) {
            ProposalDate date = new ProposalDate();
            date.setDate(dto.getDate());
            date.setProposal(proposalMap.get(dto.getProposalId()));
            date.setDinnerEvent(eventMap.get(dto.getDinnerEventId())); // Set the specific event context
            date = proposalDateRepository.save(date);
            dateMap.put(dto.getId(), date);
        }

        // Phase 5: Ratings, Votes, Messages
        List<ProposalRatingBackupDTO> ratingDTOs = (List<ProposalRatingBackupDTO>) dataMap.getOrDefault("ratings.json",
                Collections.emptyList());
        for (ProposalRatingBackupDTO dto : ratingDTOs) {
            ProposalRating rating = new ProposalRating();
            rating.setLiked(dto.isLiked());
            rating.setUser(userMap.get(dto.getUserId()));
            rating.setProposal(proposalMap.get(dto.getProposalId()));
            proposalRatingRepository.save(rating);
        }

        List<VoteBackupDTO> voteDTOs = (List<VoteBackupDTO>) dataMap.getOrDefault("votes.json",
                Collections.emptyList());
        for (VoteBackupDTO dto : voteDTOs) {
            Vote vote = new Vote();
            vote.setUser(userMap.get(dto.getUserId()));
            vote.setProposalDate(dateMap.get(dto.getProposalDateId()));
            voteRepository.save(vote);
        }

        List<DinnerEventMessageBackupDTO> messageDTOs = (List<DinnerEventMessageBackupDTO>) dataMap
                .getOrDefault("messages.json", Collections.emptyList());
        for (DinnerEventMessageBackupDTO dto : messageDTOs) {
            DinnerEventMessage message = new DinnerEventMessage();
            message.setContent(dto.getContent());
            message.setTimestamp(dto.getTimestamp());
            message.setSender(userMap.get(dto.getSenderId()));
            message.setEvent(eventMap.get(dto.getEventId()));
            messageRepository.save(message);
        }

        // Phase 6: Link Selected Dates
        for (DinnerEventBackupDTO dto : eventDTOs) {
            if (dto.getSelectedProposalDateId() != null && dateMap.containsKey(dto.getSelectedProposalDateId())) {
                DinnerEvent event = eventMap.get(dto.getId()); // Get the latest ref from map (though we saved it
                                                               // multiple times)
                // Re-fetch might be safer if we saved it in Phase 3 loop?
                // Since eventMap values are references, and we called
                // dinnerEventRepository.save(ev) in loop,
                // the reference in map should be updated if save returns the same instance, or
                // we might differ.
                // Best to re-fetch or trust Hibernate L1 cache.
                // Let's rely on eventMap having valid IDs.
                event.setSelectedProposalDate(dateMap.get(dto.getSelectedProposalDateId()));
                dinnerEventRepository.save(event);
            }
        }
    }

    private void writeEntry(ZipOutputStream zos, String filename, Object data) throws IOException {
        ZipEntry entry = new ZipEntry(filename);
        zos.putNextEntry(entry);
        objectMapper.writeValue(zos, data);
        zos.closeEntry();
    }

}
