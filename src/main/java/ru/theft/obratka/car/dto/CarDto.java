package ru.theft.obratka.car.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.theft.obratka.car.model.TypeCarBody;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarDto {
    private String carStamp;
    private String carNumber;
    private TypeCarBody carType;
    private String carLength;
    private String carWidth;
    private String carHeight;
    private String carVolume;
    private String carLoadOpacity;
}
