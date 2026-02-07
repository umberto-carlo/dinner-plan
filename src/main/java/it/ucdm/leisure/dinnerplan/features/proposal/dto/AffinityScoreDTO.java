package it.ucdm.leisure.dinnerplan.features.proposal.dto;

public class AffinityScoreDTO {
    private double totalScore;
    private double distanceScore;
    private double dietScore;
    private double popularityScore;
    private String explanation;

    public AffinityScoreDTO(double totalScore, double distanceScore, double dietScore, double popularityScore, String explanation) {
        this.totalScore = totalScore;
        this.distanceScore = distanceScore;
        this.dietScore = dietScore;
        this.popularityScore = popularityScore;
        this.explanation = explanation;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public double getDistanceScore() {
        return distanceScore;
    }

    public double getDietScore() {
        return dietScore;
    }

    public double getPopularityScore() {
        return popularityScore;
    }

    public String getExplanation() {
        return explanation;
    }
}
