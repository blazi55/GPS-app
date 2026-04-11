package gps.repository;

import gps.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {

	@Query("""
			    SELECT l
			    FROM Location l
			    JOIN FETCH l.device
			    WHERE l.device.externalId = :externalId
			    ORDER BY l.timestamp DESC
			""")
	List<Location> findLatestWithDevice(String externalId);
}
