package ru.theft.obratka.destination.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.theft.obratka.destination.model.Destination;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long> {
}
