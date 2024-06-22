package ru.theft.obratka.driver.service;

import jakarta.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.theft.obratka.driver.model.Driver;
import ru.theft.obratka.driver.repository.DriverRepository;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class DriverServiceImpl implements DriverService {

    private DriverRepository driverRepository;

    @Override
    public List<Driver> getAll() {
        List<Driver> driverList = driverRepository.findAll();
        log.info("Number of drivers in the database: {}", driverList.size());
        return driverList;
    }

    @Override
    public Driver add(Driver driver) {
        log.info("The driver has been successfully added to the database! {}", driver.toTerminal());
        if (driver.getTgId().isEmpty()) {
            throw new RuntimeException("Идентификатор не может быть пустым!");
        }
        if (driver.getFio().isEmpty()) {
            throw new RuntimeException("ФИО не может быть пустым!");
        }
        if (driver.getFio().length() > 255) {
            throw new RuntimeException("ФИО не может превышать 255 символов!");
        }
        if (driver.getTelephone().isEmpty()) {
            throw new RuntimeException("Номер телефона не может быть пустым!");
        }
        if (driver.getTelephone().length() != 11) {
            throw new RuntimeException("Номер телефона должен содержать только 11 символов!");
        }
        return driverRepository.save(driver);
    }

    @Override
    public Driver patch(Driver driver, String tgId) {
        Driver ownDriver = getByTgId(tgId)
                .orElseThrow(() -> new RuntimeException("Такого водителя не существует!"));

        if (driver.getFio() != null) {
            ownDriver.setFio(driver.getFio());
        }

        if (driver.getTelephone() != null) {
            ownDriver.setTelephone(driver.getTelephone());
        }
//        todo: сделать валидацию данных
        log.info("The driver has been successfully patched to the database! {}", driver.toTerminal());
        return driverRepository.saveAndFlush(ownDriver);
    }

    @Override
    public Optional<Driver> getByTgId(String tg) {
        return driverRepository.findAll()
                .stream()
                .filter(f -> f.getTgId().equals(tg))
                .findFirst();
    }

    @Override
    public Driver getById(String id) {
        return driverRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new NotFoundException("Такого водителя не существует!"));
    }
}
