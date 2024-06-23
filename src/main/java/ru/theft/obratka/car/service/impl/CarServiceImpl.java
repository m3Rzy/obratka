package ru.theft.obratka.car.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.theft.obratka.car.model.Car;
import ru.theft.obratka.car.repository.CarRepository;
import ru.theft.obratka.car.service.CarService;
import ru.theft.obratka.driver.model.Driver;
import ru.theft.obratka.driver.service.DriverService;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class CarServiceImpl implements CarService {
    private CarRepository carRepository;
    private DriverService driverService;

    @Override
    public Car add(Car car) {
        log.info("{} added", car);
        return carRepository.save(car);
    }

    @Override
    public List<Car> getCars() {
        List<Car> cars = carRepository.findAll();
        log.info("Count of cars: {}", cars.size());
        return cars;
    }

    @Override
    public Car getCar(long id) {

        return carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Авто с указанным идентификатором не существует!"));
    }

    @Override
    public List<Car> getDriverCars(long driverId) {
        Driver driver = driverService.getById(String.valueOf(driverId));
        List<Car> cars = carRepository.findAll()
                .stream()
                .filter(f -> f.getDriver().equals(driver))
                .toList();
        log.info("{} has found and he has cars: {}", driver, cars.size());
        return cars;
    }
}
