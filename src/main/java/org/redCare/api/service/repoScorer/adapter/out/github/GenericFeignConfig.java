package org.redCare.api.service.repoScorer.adapter.out.github;

import feign.Logger;
import feign.codec.ErrorDecoder;
import org.redCare.api.service.repoScorer.configuration.error.GenericFeignErrorDecoder;
import org.springframework.context.annotation.Bean;

public class GenericFeignConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
    return new GenericFeignErrorDecoder();
    }
}
