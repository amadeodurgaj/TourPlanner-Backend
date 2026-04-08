package at.fhtw.tourplanner.repository;

import at.fhtw.tourplanner.entity.TourLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TourLogRepository extends JpaRepository<TourLogEntity, UUID> {
    List<TourLogEntity> findByTourId(UUID tourId);
    Optional<TourLogEntity> findByIdAndTourId(UUID id, UUID tourId);
}
