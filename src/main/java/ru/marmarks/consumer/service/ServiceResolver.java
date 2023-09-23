package ru.marmarks.consumer.service;

import org.apache.avro.generic.GenericRecord;

/**
 * Интерфейс для распознования сущностей приходящих из Kafka
 */
public interface ServiceResolver {

  /**
   * Поддерживает ли сервис работу с топиком.
   *
   * @param topic топик из которого пришло сообщение.
   * @return true поддерживается\не поддерживается топик.
   */
  boolean isSupported(String topic);

  /**
   * Обработать сообщение.
   *
   * @param value сообщение.
   */
  void process(GenericRecord value);

}
