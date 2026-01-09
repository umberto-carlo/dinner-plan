package it.ucdm.leisure.dinnerplan.persistence.mongo.document;

import it.ucdm.leisure.dinnerplan.model.Vote;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "votes")
public class VoteDocument {

    @Id
    private Long id;
    private Long eventId;
    private Long proposalDateId;
    private UserDocument user;

    public VoteDocument() {
    }

    public VoteDocument(Long id, Long eventId, Long proposalDateId, UserDocument user) {
        this.id = id;
        this.eventId = eventId;
        this.proposalDateId = proposalDateId;
        this.user = user;
    }

    public Vote toDomain() {
        return new Vote(id, user != null ? user.toDomain() : null, null);
    }

    public static VoteDocument fromDomain(Vote vote, Long eventId, Long proposalDateId) {
        return new VoteDocument(
                vote.getId(),
                eventId,
                proposalDateId,
                vote.getUser() != null ? UserDocument.fromDomain(vote.getUser()) : null);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Long getProposalDateId() {
        return proposalDateId;
    }

    public void setProposalDateId(Long proposalDateId) {
        this.proposalDateId = proposalDateId;
    }

    public UserDocument getUser() {
        return user;
    }

    public void setUser(UserDocument user) {
        this.user = user;
    }
}
