package ru.theft.obratka.driver.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/own")
    public Driver getDriverById(@RequestParam String id) {
        return driverService.getById(id);
    }
}
