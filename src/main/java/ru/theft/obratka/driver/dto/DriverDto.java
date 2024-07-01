package ru.theft.obratka.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DriverDto {
    private String surname;
    private String name;
    private String patronymic;
    private String telephone;
}
