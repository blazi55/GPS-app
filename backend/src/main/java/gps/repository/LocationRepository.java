package gps.repository;

import gps.entity.Location;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {

	@Query("""
			SELECT l
			FROM Location l
			JOIN FETCH l.device d
			WHERE d.externalId = :externalId
			ORDER BY l.timestamp DESC
			""")
	List<Location> findByDeviceExternalIdOrderByTimestampDesc(
			@Param("externalId") String externalId,
			Pageable pageable
	);

	default Optional<Location> findLatestWithDevice(String externalId) {
		return findByDeviceExternalIdOrderByTimestampDesc(externalId, Pageable.ofSize(1))
				.stream()
				.findFirst();
	}

	@Query("""
			SELECT l
			FROM Location l
			JOIN FETCH l.device d
			WHERE d.externalId = :externalId
			  AND (:from IS NULL OR l.timestamp >= :from)
			  AND (:to IS NULL OR l.timestamp <= :to)
			ORDER BY l.timestamp ASC
			""")
	List<Location> findHistory(
			@Param("externalId") String externalId,
			@Param("from") Instant from,
			@Param("to") Instant to
	);

	@Query("""
			SELECT l
			FROM Location l
			JOIN FETCH l.device d
			WHERE l.timestamp = (
			    SELECT MAX(l2.timestamp)
			    FROM Location l2
			    WHERE l2.device = d
			)
			ORDER BY d.externalId ASC
			""")
	List<Location> findLatestForAllDevices();
}
