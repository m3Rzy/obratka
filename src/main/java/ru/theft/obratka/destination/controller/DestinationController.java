package ru.theft.obratka.destination.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.theft.obratka.destination.model.Destination;
import ru.theft.obratka.destination.service.DestinationService;

import java.util.List;

@RestController
@RequestMapping("/arrival")
@AllArgsConstructor
public class DestinationController {
    public DestinationService destinationService;

    @GetMapping
    public List<Destination> getAllDestinations() {
        return destinationService.getAll();
    }

    @GetMapping("/{id}")
    public Destination getDestinationById(@PathVariable long id) {
        return destinationService.getById(id);
    }

}
