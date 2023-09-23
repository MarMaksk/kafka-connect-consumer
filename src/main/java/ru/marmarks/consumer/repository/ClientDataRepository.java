package ru.marmarks.consumer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.marmarks.consumer.entity.ClientData;

@Repository
public interface ClientDataRepository extends JpaRepository<ClientData, Long> {

}
