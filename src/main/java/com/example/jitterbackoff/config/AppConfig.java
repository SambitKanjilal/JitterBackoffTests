package com.example.jitterbackoff.config;

import com.example.jitterbackoff.backoff.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AppConfig {

    @Bean public NoJitterStrategy noJitterStrategy()                     { return new NoJitterStrategy(); }
    @Bean public FullJitterStrategy fullJitterStrategy()                 { return new FullJitterStrategy(); }
    @Bean public EqualJitterStrategy equalJitterStrategy()               { return new EqualJitterStrategy(); }
    @Bean public DecorrelatedJitterStrategy decorrelatedJitterStrategy() { return new DecorrelatedJitterStrategy(); }

    /** No-Jitter first so its metrics serve as the comparison baseline in all runs. */
    @Bean
    public List<BackoffStrategy> orderedStrategies(
            NoJitterStrategy a, FullJitterStrategy b,
            EqualJitterStrategy c, DecorrelatedJitterStrategy d) {
        return List.of(a, b, c, d);
    }
}
