package ru.theft.obratka.car.service;

import ru.theft.obratka.car.model.Car;

import java.util.List;
import java.util.Optional;

public interface CarService {
    Car add(Car car);

    List<Car> getCarsByDriverId(long id);

    List<Car> getCars();

    Car getCar(long id);

    Optional<Car> getCarByCarNumber(String carNumber);

    List<Car> getDriverCars(long driverId);
}
