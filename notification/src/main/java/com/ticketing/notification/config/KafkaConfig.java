package com.ticketing.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.RecordMessageConverter;
import org.springframework.kafka.support.converter.StringJacksonJsonMessageConverter;
import tools.jackson.databind.json.JsonMapper;

/**
 * The notification service only consumes topics owned by other services, so it
 * declares no topics of its own. This single converter lets the String-based
 * consumer bind incoming JSON payloads into the typed event records
 * (Spring Boot 4 / Jackson 3).
 */
@Configuration
public class KafkaConfig {

    @Bean
    public RecordMessageConverter jsonMessageConverter(JsonMapper jsonMapper) {
        return new StringJacksonJsonMessageConverter(jsonMapper);
    }
}
