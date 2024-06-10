package ru.theft.obratka.destination.model;

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
@Table(name = "destinations")
public class Destination {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "destination_from", nullable = false)
    private String fromRoute;

    @Column(name = "destination_to", nullable = false)
    private String toRoute;

    @Column(name = "destination_date_arrival", nullable = false)
    private String dateOfArrival;

    @CreationTimestamp
    @Column(name = "destination_created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "driver_id", referencedColumnName = "id", nullable = false)
    private Driver driver;
}
