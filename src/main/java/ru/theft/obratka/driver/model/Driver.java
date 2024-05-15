package ru.theft.obratka.driver.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "drivers")
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "driver_telegram_id", nullable = false, unique = true)
    private String tgId;

    @Column(name = "driver_fio", nullable = false)
    private String fio;

    @Column(name = "driver_telephone", unique = true, nullable = false)
    private String telephone;

    @Column(name = "driver_type_car_body", nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeCarBody typeCarBody;

    @Column(name = "driver_car_body_dimensions", nullable = false)
    private String dimensions;

    @Column(name = "driver_load_opacity", nullable = false)
    private int loadOpacity;
}
