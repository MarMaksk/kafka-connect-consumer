package ru.marmarks.consumer.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.marmarks.consumer.entity.ClientData;
import ru.marmarks.consumer.repository.ClientDataRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientDataService implements ServiceResolver {

  private final ClientDataRepository clientDataRepository;

  // Топик, который сервис может обрабатывать
  @Value("${kafka.services.personal-data}")
  private String topic;

  @Override
  public boolean isSupported(String topic) {
    return Objects.equals(this.topic, topic);
  }

  @Override
  public void process(GenericRecord value) {
    Long id = Long.valueOf(String.valueOf(value.get("id")));
    String bankBic = String.valueOf(value.get("bank_bic"));
    String bankName = String.valueOf(value.get("bank_name"));
    String lastUpdate = String.valueOf(value.get("last_update"));
    long millis = Long.parseLong(lastUpdate);
    Instant instant = Instant.ofEpochMilli(millis);
    LocalDateTime lastUpdateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();

    // Подготовим новую сущность на основе приходящих данных
    ClientData clientData = new ClientData();
    clientData.setBankBic(bankBic);
    clientData.setFio("Рауль " + id);
    clientData.setBankName(bankName);
    clientData.setCorrelationId(id);
    clientData.setLastUpdate(lastUpdateTime);

    ClientData save = clientDataRepository.save(clientData);
    log.info("Данные клиента сохранены: {}", save);
  }

}
