package org.redCare.api.service.repoScorer.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "management")
@Data
public class RepoScorerApplicationProperties {

    private Scoring scoring = new Scoring();

    @Data
    public static class Scoring {
        private Double starWeight = 0.50;
        private Double forkWeight = 0.30;
        private Double recencyWeight = 0.20;
    }
}
