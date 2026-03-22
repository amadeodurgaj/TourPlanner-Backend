package at.fhtw.tourplanner.repository;

import at.fhtw.tourplanner.entity.TourEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TourRepository extends JpaRepository<TourEntity, UUID> {
    List<TourEntity> findByUserId(UUID userId);
    Optional<TourEntity> findByIdAndUserId(UUID id, UUID userId);
    void deleteByIdAndUserId(UUID id, UUID userId);
}
