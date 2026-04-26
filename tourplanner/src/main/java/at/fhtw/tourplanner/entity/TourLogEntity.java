package at.fhtw.tourplanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
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

    @NotNull
    private LocalDateTime dateTime;

    @NotBlank
    @Size(max = 5000)
    @Column(nullable = false, length = 5000)
    private String comment;

    @NotNull
    @Enumerated(EnumType.STRING)     // you were missing this!
    @Column(nullable = false)
    private Difficulty difficulty;

    @NotNull
    @PositiveOrZero
    private Double totalDistance;

    @NotNull
    @PositiveOrZero
    private Integer totalTime;       // minutes is simpler, Duration has DB mapping issues

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @ManyToOne(fetch = FetchType.LAZY)   // add LAZY like TourEntity
    @JoinColumn(name = "tour_id", nullable = false)
    private TourEntity tour;

    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private LocalDateTime updatedAt;
}
