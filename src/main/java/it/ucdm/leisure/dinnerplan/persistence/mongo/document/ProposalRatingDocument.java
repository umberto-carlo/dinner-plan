package it.ucdm.leisure.dinnerplan.persistence.mongo.document;

import it.ucdm.leisure.dinnerplan.model.ProposalRating;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "proposal_ratings")
public class ProposalRatingDocument {

    @Id
    private Long id;
    private Long proposalId;
    private UserDocument user;
    private boolean isLiked;

    public ProposalRatingDocument() {
    }

    public ProposalRatingDocument(Long id, Long proposalId, UserDocument user, boolean isLiked) {
        this.id = id;
        this.proposalId = proposalId;
        this.user = user;
        this.isLiked = isLiked;
    }

    public ProposalRating toDomain() {
        return new ProposalRating(id, isLiked, user != null ? user.toDomain() : null, null);
    }

    public static ProposalRatingDocument fromDomain(ProposalRating rating, Long proposalId) {
        return new ProposalRatingDocument(
                rating.getId(),
                proposalId,
                rating.getUser() != null ? UserDocument.fromDomain(rating.getUser()) : null,
                rating.isLiked());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProposalId() {
        return proposalId;
    }

    public void setProposalId(Long proposalId) {
        this.proposalId = proposalId;
    }

    public UserDocument getUser() {
        return user;
    }

    public void setUser(UserDocument user) {
        this.user = user;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }
}
