package ru.theft.obratka.car.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import ru.theft.obratka.driver.model.Driver;

import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name = "car_weight", nullable = false)
    private String carWeight;

    @Column(name = "car_height", nullable = false)
    private String carHeight;

    @Column(name = "car_volume", nullable = false)
    private String carVolume;

    @Column(name = "car_type_of_loading", nullable = false)
    List<TypeOfLoadingCar> typeOfLoadingCars;

    @Column(name = "car_load_opacity", nullable = false)
    private String carLoadOpacity;

    @ManyToOne
    @JoinColumn(name = "driver_id", referencedColumnName = "id")
    private Driver driver;

    @CreationTimestamp
    @Column(name = "car_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
