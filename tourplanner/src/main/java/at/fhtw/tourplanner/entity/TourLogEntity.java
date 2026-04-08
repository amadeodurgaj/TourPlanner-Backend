package at.fhtw.tourplanner.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tour_logs")          // explicit table name like TourEntity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TourLogEntity {

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)  // UUID like TourEntity, not Long
    private UUID id;

    private LocalDateTime dateTime;
    private String comment;

    @Enumerated(EnumType.STRING)     // you were missing this!
    private Difficulty difficulty;

    private Double totalDistance;
    private Integer totalTime;       // minutes is simpler, Duration has DB mapping issues

    private Integer rating;

    @ManyToOne(fetch = FetchType.LAZY)   // add LAZY like TourEntity
    @JoinColumn(name = "tour_id", nullable = false)
    private TourEntity tour;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}