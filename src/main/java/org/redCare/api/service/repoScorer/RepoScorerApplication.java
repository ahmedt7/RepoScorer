package org.redCare.api.service.repoScorer;

import org.redCare.api.service.repoScorer.configuration.RepoScorerApplicationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * Spring Boot entry point for the repository scoring service.
 *
 * <p>The application enables OpenFeign clients, method-level authorization, and
 * component scanning across the service package hierarchy.</p>
 */
@SpringBootApplication
@EnableFeignClients(basePackages = {"org.redCare.api.service.repoScorer"})
@ComponentScan(basePackages = {"org.redCare"})
@EnableMethodSecurity
@ConfigurationPropertiesScan(
        basePackageClasses = {RepoScorerApplicationProperties.class})
public class RepoScorerApplication {
    /**
     * Starts the repository scoring service.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(RepoScorerApplication.class, args);
    }
}
