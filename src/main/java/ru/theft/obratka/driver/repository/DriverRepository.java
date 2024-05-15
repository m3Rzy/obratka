package ru.theft.obratka.driver.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.theft.obratka.driver.model.Driver;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
}
