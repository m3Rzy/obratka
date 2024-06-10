package ru.theft.obratka.destination.service;

import ru.theft.obratka.destination.model.Destination;

import java.util.List;

public interface DestinationService {
    Destination add(Destination destination, String tgId);

    Destination patch(Destination destination, long id);

    List<Destination> getAll();

    Destination getById(long id);
}
