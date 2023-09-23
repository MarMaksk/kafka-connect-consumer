package ru.marmarks.consumer.config;

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.RestService;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import ru.marmarks.consumer.config.properties.SchemaRegistryProperties;

/**
 * Конфигурация Kafka.
 */
@Configuration
@EnableKafka
@RequiredArgsConstructor
@EnableConfigurationProperties(SchemaRegistryProperties.class)
public class KafkaConfiguration {

  private static final int DEFAULT_CACHE_CAPACITY = 200;
  private static final String DLT_TOPIC_SUFFIX = ".dlt";
  private final ConsumerFactory<String, GenericRecord> consumerFactory;
  private final ProducerFactory<Object, Object> producerFactory;

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, GenericRecord> objectKafkaListenerContainerFactory(
      DefaultErrorHandler errorHandler
  ) {
    ConcurrentKafkaListenerContainerFactory<String, GenericRecord> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    factory.setCommonErrorHandler(errorHandler);
    return factory;
  }

  /**
   * Клиент для кеширования схем
   */
  @Bean
  public CachedSchemaRegistryClient cachedSchemaRegistryClient(
      SchemaRegistryProperties schemaRegistryProperties) {
    // Сервис для коммуникацией со Schema Registry
    RestService restService = new RestService(schemaRegistryProperties.urls());
    //Также передаём размер кэша схем
    return new CachedSchemaRegistryClient(restService, DEFAULT_CACHE_CAPACITY);
  }

  @Bean
  public KafkaTemplate<Object, Object> kafkaTemplate() {
    return new KafkaTemplate<>(producerFactory);
  }

  /**
   * Публикатор в dead-letter topic.
   */
  @Bean
  public DeadLetterPublishingRecoverer publisher(KafkaTemplate<Object, Object> bytesTemplate) {
    //  Определяем логику выбора партиции для отправки сообщения в DLT.
    //  В данном случае, создаём новый объект TopicPartition, используя имя топика (consumerRecord.topic()) и добавляя суффикс DLT_TOPIC_SUFFIX,
    //  а также номер партиции (consumerRecord.partition()).
    //  Следовательно в DLT топике должно быть столько партиций сколько и в топике откуда читаем
    return new DeadLetterPublishingRecoverer(bytesTemplate, (consumerRecord, exception) ->
        new TopicPartition(consumerRecord.topic() + DLT_TOPIC_SUFFIX, consumerRecord.partition()));
  }

  /**
   * Обработчик исключений при получении сообщений из kafka по умолчанию.
   */
  @Bean
  public DefaultErrorHandler errorHandler(
      DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
    final var handler = new DefaultErrorHandler(deadLetterPublishingRecoverer);
    // Обрабатываем любые исключения и отправляем в DLT
    handler.addNotRetryableExceptions(Exception.class);
    return handler;
  }
}
