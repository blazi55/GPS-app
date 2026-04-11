package gps.service;

import gps.dto.LocationDto;
import gps.entity.Device;
import gps.entity.Location;
import gps.repository.DeviceRepository;
import gps.repository.LocationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LocationService {

	private final DeviceRepository deviceRepo;
	private final LocationRepository locationRepo;

	@Transactional
	public void handleIncomingLocation(final LocationDto dto) {
		if (dto.getLatitude() < -90 || dto.getLatitude() > 90) {
			throw new IllegalArgumentException("Invalid latitude");
		}

		if (dto.getLongitude() < -180 || dto.getLongitude() > 180) {
			throw new IllegalArgumentException("Invalid longitude");
		}

		if (dto.getDeviceExternalId() == null || dto.getDeviceExternalId().isBlank()) {
			throw new IllegalArgumentException("DeviceExternalId is required");
		}

		final Device device = deviceRepo
				.findByExternalId(dto.getDeviceExternalId())
				.orElseThrow(() -> new RuntimeException(
						"Device not found for externalId: " + dto.getDeviceExternalId()
				));

		final Location loc = new Location();
		loc.setDevice(device);
		loc.setLatitude(dto.getLatitude());
		loc.setLongitude(dto.getLongitude());
		loc.setTimestamp(
				dto.getTimestamp() != null ? dto.getTimestamp() : Instant.now()
		);

		locationRepo.save(loc);
	}

	public LocationDto getLatest(String externalId) {
		return locationRepo.findLatestWithDevice(externalId)
				.stream()
				.findFirst()
				.map(this::mapToDto)
				.orElseThrow();
	}

	private LocationDto mapToDto(final Location location) {
		final LocationDto dto = new LocationDto();
		dto.setDeviceExternalId(location.getDevice().getExternalId());
		dto.setLongitude(location.getLongitude());
		dto.setLatitude(location.getLatitude());
		dto.setTimestamp(location.getTimestamp());
		return dto;
	}
}
