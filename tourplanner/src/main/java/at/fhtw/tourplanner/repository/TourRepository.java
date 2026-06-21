package at.fhtw.tourplanner.repository;

import at.fhtw.tourplanner.DTO.TourSummaryDTO;
import at.fhtw.tourplanner.entity.TourEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TourRepository extends JpaRepository<TourEntity, UUID> {
    List<TourEntity> findByUserId(UUID userId);

    @Query("SELECT new at.fhtw.tourplanner.DTO.TourSummaryDTO(t.id, t.name, t.distance) FROM TourEntity t WHERE t.user.id = :userId")
    List<TourSummaryDTO> findSummariesByUserId(@Param("userId") UUID userId);

    Optional<TourEntity> findByIdAndUserId(UUID id, UUID userId);
    void deleteByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT t FROM TourEntity t WHERE t.user.id = :userId " +
           "AND (LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR CAST(t.popularityScore AS string) LIKE CONCAT('%', :query, '%') " +
           "OR CAST(t.childFriendliness AS string) LIKE CONCAT('%', :query, '%'))")
    List<TourEntity> searchByUserId(@Param("userId") UUID userId, @Param("query") String query);
}
