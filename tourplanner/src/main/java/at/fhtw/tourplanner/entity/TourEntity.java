package at.fhtw.tourplanner.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "tours")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TourEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String name;
    private String description;
    private String transportType;
    
    private String fromLocation;
    private Double fromLatitude;
    private Double fromLongitude;
    
    private String toLocation;
    private Double toLatitude;
    private Double toLongitude;
    
    private double distance;
    private String estimatedTime;
    
    @Column(columnDefinition = "jsonb")
    private String routeInfo;
    
    private int childFriendliness;
    private int popularityScore;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}