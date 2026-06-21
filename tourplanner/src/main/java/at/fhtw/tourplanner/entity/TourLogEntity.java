package at.fhtw.tourplanner.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tour_logs", indexes = {
    @Index(name = "idx_tourlog_tour_comment", columnList = "tour_id, comment")
})
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TourLogEntity {

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDateTime dateTime;

    @Column(nullable = false, length = 5000)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    private Double totalDistance;

    private Integer totalTime;

    private Integer rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tour_id", nullable = false)
    private TourEntity tour;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
