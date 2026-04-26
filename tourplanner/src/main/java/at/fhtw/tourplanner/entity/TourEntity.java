package at.fhtw.tourplanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
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
    
    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Size(max = 5000)
    @Column(nullable = false, length = 5000)
    private String description;

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "foot|bike|running|car")
    @Column(nullable = false, length = 50)
    private String transportType;
    
    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String fromLocation;

    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double fromLatitude;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double fromLongitude;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String toLocation;

    @NotNull
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double toLatitude;

    @NotNull
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double toLongitude;
    
    @PositiveOrZero
    private double distance;

    @Size(max = 100)
    private String estimatedTime;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> routeInfo;
    
    @PositiveOrZero
    private int childFriendliness;

    @PositiveOrZero
    private int popularityScore;
    
    @Column(columnDefinition = "TEXT")
    @Size(max = 2000)
    private String imagePath;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    
    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private LocalDateTime updatedAt;
}
