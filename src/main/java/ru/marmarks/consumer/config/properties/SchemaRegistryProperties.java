package ru.marmarks.consumer.config.properties;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.kafka.schema.registry")
public record SchemaRegistryProperties(List<String> urls) {

}
