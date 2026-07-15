package org.redCare.api.service.repoScorer.domain.model;

import java.util.Objects;

/**
 * Weight configuration for repository popularity scoring components.
 *
 * @param starWeight contribution of normalized stars
 * @param forkWeight contribution of normalized forks
 * @param recencyWeight contribution of update recency
 */
public record RepositoryScoringWeights(double starWeight, double forkWeight, double recencyWeight) {

    /**
     * Creates scoring weights from nullable configuration wrapper values.
     *
     * @param starWeight configured star weight
     * @param forkWeight configured fork weight
     * @param recencyWeight configured recency weight
     * @return immutable scoring weights
     */
    public static RepositoryScoringWeights of(Double starWeight, Double forkWeight, Double recencyWeight) {
        return new RepositoryScoringWeights(
                Objects.requireNonNull(starWeight, "starWeight must not be null"),
                Objects.requireNonNull(forkWeight, "forkWeight must not be null"),
                Objects.requireNonNull(recencyWeight, "recencyWeight must not be null"));
    }
}
