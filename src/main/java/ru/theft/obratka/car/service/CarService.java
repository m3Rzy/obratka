package ru.theft.obratka.car.service;

import ru.theft.obratka.car.model.Car;

import java.util.List;

public interface CarService {
    Car add(Car car);

    List<Car> getCars();

    Car getCar(long id);

    List<Car> getDriverCars(long driverId);
}
