package ru.theft.obratka.car.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import ru.theft.obratka.driver.model.Driver;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "cars")
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "car_stamp", nullable = false)
    private String carStamp;

    @Column(name = "car_number", nullable = false, unique = true)
    private String carNumber;

    @Column(name = "car_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeCarBody typeCarBody;

    @Column(name = "car_length", nullable = false)
    private String carLength;

    @Column(name = "car_width", nullable = false)
    private String carWidth;

    @Column(name = "car_height", nullable = false)
    private String carHeight;

    @Column(name = "car_volume", nullable = false)
    private String carVolume;

    @Column(name = "car_load_opacity", nullable = false)
    private String carLoadOpacity;

    @ManyToOne
    @JoinColumn(name = "driver_id", referencedColumnName = "id")
    private Driver driver;

    @CreationTimestamp
    @Column(name = "car_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public String toTerminal() {

        String s_stamp = "Марка:";
        String s_number = "Госномер:";
        String s_type = "Тип кузова:";
        String s_length = "Длина кузова:";
        String s_width = "Ширина кузова:";
        String s_height = "Высота кузова:";
        String s_volume = "Объём кузова:";
        String s_loadOpacity = "Грузоподъемность кузова:";
        int totalWidth = 40;
        return String.format("```\n" +
                        "%s%" + (totalWidth - s_stamp.length()) + "s\n" +
                        "%s%" + (totalWidth - s_number.length()) + "s\n" +
                        "%s%" + (totalWidth - s_type.length()) + "s\n" +
                        "%s%" + (totalWidth - s_length.length()) + "s\n" +
                        "%s%" + (totalWidth - s_width.length()) + "s\n" +
                        "%s%" + (totalWidth - s_height.length()) + "s\n" +
                        "%s%" + (totalWidth - s_volume.length()) + "s\n" +
                        "%s%" + (totalWidth - s_loadOpacity.length()) + "s\n" +
                        "```",
                s_stamp, carStamp,
                s_number, carNumber,
                s_type, typeCarBody.getTitle(),
                s_length, carLength + " м",
                s_width, carWidth + " м",
                s_height, carHeight + " м",
                s_volume, carVolume + " м³",
                s_loadOpacity, carLoadOpacity + " кг.");
    }
}
