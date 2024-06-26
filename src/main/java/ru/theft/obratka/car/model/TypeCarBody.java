package ru.theft.obratka.car.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TypeCarBody {
    VAN("Фургон"),
    TENT("Тент"),
    ISOTHERMAL("Изотермический"),
    OPEN("Открытый");

    private final String title;
}
