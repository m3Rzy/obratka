package ru.theft.obratka.driver.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.theft.obratka.driver.model.Driver;
import ru.theft.obratka.driver.service.DriverService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/driver")
public class DriverController {
    public DriverService driverService;

    @GetMapping
    public List<Driver> getAllDrivers() {
        return driverService.getAll();
    }

    @GetMapping("/{id}")
    public Driver getDriverById(@PathVariable long id) {
        return driverService.getById(id);
    }
}
