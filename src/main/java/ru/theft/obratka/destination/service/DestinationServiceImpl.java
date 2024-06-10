package ru.theft.obratka.destination.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.theft.obratka.destination.model.Destination;
import ru.theft.obratka.destination.repository.DestinationRepository;
import ru.theft.obratka.driver.model.Driver;
import ru.theft.obratka.driver.service.DriverService;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class DestinationServiceImpl implements DestinationService {

    private DestinationRepository destinationRepository;
    private DriverService driverService;

    @Override
    public Destination add(Destination destination, String tgId) {
        Driver driver = driverService.getByTgId(tgId)
                .orElseThrow(() -> new RuntimeException("Водителя не существует!"));
        destination.setDriver(driver);
        log.info("Destination {} created.", destination);
        return destinationRepository.save(destination);
    }

    @Override
    public Destination patch(Destination destination, long id) {
        Destination ownDestination = getById(id);
        ownDestination.setFromRoute(destination.getFromRoute());
        ownDestination.setToRoute(destination.getToRoute());
        ownDestination.setDateOfArrival(destination.getDateOfArrival());
        return destinationRepository.saveAndFlush(ownDestination);
    }

    @Override
    public List<Destination> getAll() {
        List<Destination> destinations = destinationRepository.findAll();
        log.info("Count of destinations: {}", destinations.size());
        return destinations;
    }

    @Override
    public Destination getById(long id) {
        Destination destination = destinationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Такого маршрута не существует!"));
        log.info("Destination found by id {}.", id);
        return destination;
    }
}
