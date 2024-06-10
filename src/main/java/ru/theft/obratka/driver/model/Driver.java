package ru.theft.obratka.driver.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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

    @CreationTimestamp
    @Column(name = "driver_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Override
    public String toString() {
        String s_fio = "ФИО:";
        String s_telephone = "Номер телефона для связи:";
        String s_typeCarBody = "Тип авто:";
        String s_dimensions = "Габариты:";
        String s_loadOpacity = "Грузоподъёмность (кг):";
        int totalWidth = 50;
        return String.format("```\n" +
                        "%s%" + (totalWidth - s_fio.length()) + "s\n" +
                        "%s%" + (totalWidth - s_telephone.length()) + "s\n" +
                        "%s%" + (totalWidth - s_typeCarBody.length()) + "s\n" +
                        "%s%" + (totalWidth - s_dimensions.length()) + "s\n" +
                        "%s%" + (totalWidth - s_loadOpacity.length()) + "s\n" +
                        "```",
                s_fio, fio, s_telephone, "+" + telephone, s_typeCarBody, typeCarBody.getTitle(),
                s_dimensions, dimensions,
                s_loadOpacity, loadOpacity);
    }
}
