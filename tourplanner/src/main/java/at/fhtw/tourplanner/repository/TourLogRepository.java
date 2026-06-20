package at.fhtw.tourplanner.repository;

import at.fhtw.tourplanner.entity.TourLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TourLogRepository extends JpaRepository<TourLogEntity, UUID> {
    List<TourLogEntity> findByTourId(UUID tourId);
    Optional<TourLogEntity> findByIdAndTourId(UUID id, UUID tourId);
    long countByTourId(UUID tourId);
    List<TourLogEntity> findByTourIdAndCommentContainingIgnoreCase(UUID tourId, String query);

    @Query("SELECT l FROM TourLogEntity l WHERE l.tour.id = :tourId AND " +
           "(LOWER(l.comment) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(l.difficulty) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<TourLogEntity> searchByTourId(@Param("tourId") UUID tourId, @Param("query") String query);
}
