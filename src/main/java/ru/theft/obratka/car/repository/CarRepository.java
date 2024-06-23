package ru.theft.obratka.car.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.theft.obratka.car.model.Car;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
}
