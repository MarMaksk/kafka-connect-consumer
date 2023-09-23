package ru.marmarks.consumer.consumer;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.marmarks.consumer.service.ServiceResolver;

/**
 * Kafka consumers.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerListeners {

  // Получим сервисы, которые смогут обрабатывать приходящие сообщения
  private final List<ServiceResolver> serviceResolverList;

  // Определяем топики из которых будем читать. Их может быть несколько. Также определим конфигурацию листнера
  @KafkaListener(topics = "#{'${kafka.topics.personal-data}'.split(',')}", containerFactory = "objectKafkaListenerContainerFactory")
  // Принимаем абстрактный класс GenericRecord. Мы сможем обработать только то сообщение, для которого определена схема в Schema Regestry
  void readOrganizationConnectorMessages(ConsumerRecord<String, GenericRecord> message) {
    readMessage(message);
  }

  private void readMessage(ConsumerRecord<String, GenericRecord> message) {
    String topic = message.topic();
    long offset = message.offset();
    log.info("Сообщение из топика: {} offset: {}", topic, offset);

    // Получаем само сообщение
    GenericRecord value = message.value();

    if (value == null) {
      log.info("Пустое сообщение из топика: {} offset: {}", topic, offset);
      return;
    }

    // Находим сервис, который сможет обработать сообщение
    ServiceResolver resolver = serviceResolverList.stream()
        .filter(it -> it.isSupported(topic))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Сервис не найден для топика: " + topic));

    // Передаём сообщение на обработку
    resolver.process(value);

    log.info("Сообщение обработано: топик {} offset {}", topic, offset);
  }

}