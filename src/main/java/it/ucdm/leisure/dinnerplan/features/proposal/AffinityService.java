package it.ucdm.leisure.dinnerplan.features.proposal;

import it.ucdm.leisure.dinnerplan.features.event.DinnerEvent;
import it.ucdm.leisure.dinnerplan.features.proposal.dto.AffinityScoreDTO;
import it.ucdm.leisure.dinnerplan.features.user.DietaryPreference;
import it.ucdm.leisure.dinnerplan.features.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AffinityService {

    @Value("${app.scoring.weight.distance:0.40}")
    private double distanceWeight;

    @Value("${app.scoring.weight.diet:0.40}")
    private double dietWeight;

    @Value("${app.scoring.weight.popularity:0.20}")
    private double popularityWeight;

    @Value("${app.scoring.max-distance-km:50.0}")
    private double maxDistanceKm;

    public AffinityScoreDTO calculateAffinity(Proposal proposal, DinnerEvent event) {
        List<User> participants = event.getParticipants();
        if (participants == null || participants.isEmpty()) {
            return new AffinityScoreDTO(0, 0, 0, 0, "No participants");
        }

        // 1. Distance Score
        double distanceScore = calculateDistanceScore(proposal, participants, event.getOrganizer());

        // 2. Diet Score
        double dietScore = calculateDietScore(proposal, participants, event.getOrganizer());

        // 3. Popularity Score (Historical)
        double popularityScore = calculatePopularityScore(proposal);

        // Total
        double total = (distanceScore * distanceWeight) + (dietScore * dietWeight) + (popularityScore * popularityWeight);
        // Normalize to 0-100 scale for display
        double finalScore = Math.min(100.0, Math.max(0.0, total * 100));

        String explanation = String.format("Dist: %.0f%%, Diet: %.0f%%, Pop: %.0f%%", 
                distanceScore * 100, dietScore * 100, popularityScore * 100);

        return new AffinityScoreDTO(finalScore, distanceScore, dietScore, popularityScore, explanation);
    }

    private double calculateDistanceScore(Proposal proposal, List<User> participants, User organizer) {
        if (proposal.getLatitude() == null || proposal.getLongitude() == null) {
            return 0.0;
        }

        double totalDist = 0;
        int count = 0;

        // Include organizer
        if (organizer.getLatitude() != null && organizer.getLongitude() != null) {
            totalDist += calculateHaversineDistance(organizer.getLatitude(), organizer.getLongitude(),
                    proposal.getLatitude(), proposal.getLongitude());
            count++;
        }

        for (User p : participants) {
            if (p.getLatitude() != null && p.getLongitude() != null) {
                totalDist += calculateHaversineDistance(p.getLatitude(), p.getLongitude(),
                        proposal.getLatitude(), proposal.getLongitude());
                count++;
            }
        }

        if (count == 0) return 0.5; // Neutral if no user locations known

        double avgDist = totalDist / count;
        
        // Linear decay: 0km = 1.0, maxDistanceKm = 0.0
        if (avgDist >= maxDistanceKm) return 0.0;
        return 1.0 - (avgDist / maxDistanceKm);
    }

    private double calculateDietScore(Proposal proposal, List<User> participants, User organizer) {
        Set<DietaryPreference> supported = proposal.getDietaryPreferences();
        int totalUsers = participants.size() + 1; // + organizer
        int satisfiedUsers = 0;

        // Check organizer
        if (isDietSatisfied(organizer, supported)) satisfiedUsers++;

        // Check participants
        for (User p : participants) {
            if (isDietSatisfied(p, supported)) satisfiedUsers++;
        }

        return (double) satisfiedUsers / totalUsers;
    }

    private boolean isDietSatisfied(User user, Set<DietaryPreference> supported) {
        if (user.getDietaryPreference() == DietaryPreference.OMNIVORE) return true;
        return supported.contains(user.getDietaryPreference());
    }

    private double calculatePopularityScore(Proposal proposal) {
        if (proposal.getRatings() == null || proposal.getRatings().isEmpty()) {
            return 0.5; // Neutral start for new places
        }

        long likes = proposal.getRatings().stream().filter(ProposalRating::isLiked).count();
        long total = proposal.getRatings().size();

        // Simple ratio
        return (double) likes / total;
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
