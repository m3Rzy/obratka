package ru.theft.obratka.car.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TypeOfLoadingCar {
    UPPER("Верхняя"),
    BACK("Задняя"),
    SIDE("Боковая");

    private final String desc;
}
