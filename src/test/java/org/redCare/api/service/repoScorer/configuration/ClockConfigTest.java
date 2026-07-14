package org.redCare.api.service.repoScorer.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import org.junit.jupiter.api.Test;

class ClockConfigTest {

    @Test
    void createsUtcSystemClock() {
        Clock clock = new ClockConfig().clock();

        assertThat(clock.getZone()).isEqualTo(Clock.systemUTC().getZone());
    }
}
