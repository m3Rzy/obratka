package ru.theft.obratka.driver.service;

import ru.theft.obratka.driver.model.Driver;

import java.util.List;

public interface DriverService {
    List<Driver> getAll();

    Driver add(Driver driver);

    Driver patch(Driver driver, String tgId);

    Driver getByTgId(String tg);

    Driver getById(String id);
}
