package org.redCare.api.service.repoScorer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableFeignClients(basePackages = {"org.redCare.api.service.repoScorer"})
@ComponentScan(basePackages = {"org.redCare"})
@EnableMethodSecurity
public class RepoScorerApplication {
    public static void main(String[] args) {
        SpringApplication.run(RepoScorerApplication.class, args);
    }
}
