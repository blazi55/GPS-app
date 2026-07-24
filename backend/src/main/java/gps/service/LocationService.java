package gps.service;

import gps.dto.LocationDto;
import gps.dto.TrackSummaryDto;
import gps.entity.Device;
import gps.entity.Location;
import gps.exception.NotFoundException;
import gps.i18n.MessageKeys;
import gps.i18n.MessageService;
import gps.repository.DeviceRepository;
import gps.repository.LocationRepository;
import gps.util.GeoUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocationService {

	private final DeviceRepository deviceRepo;
	private final LocationRepository locationRepo;
	private final MessageService messages;

	@Transactional
	public void handleIncomingLocation(final LocationDto dto) {
		if (!GeoUtils.isValidLatitude(dto.getLatitude())) {
			log.error("Invalid latitude: {}", dto.getLatitude());
			throw new AmqpRejectAndDontRequeueException(
					messages.get(MessageKeys.LOCATION_LATITUDE_INVALID)
			);
		}

		if (!GeoUtils.isValidLongitude(dto.getLongitude())) {
			log.error("Invalid longitude: {}", dto.getLongitude());
			throw new AmqpRejectAndDontRequeueException(
					messages.get(MessageKeys.LOCATION_LONGITUDE_INVALID)
			);
		}

		if (dto.getDeviceExternalId() == null || dto.getDeviceExternalId().isBlank()) {
			log.error("DeviceExternalId is required");
			throw new AmqpRejectAndDontRequeueException(
					messages.get(MessageKeys.LOCATION_DEVICE_EXTERNAL_ID_REQUIRED)
			);
		}

		final Device device = deviceRepo
				.findByExternalId(dto.getDeviceExternalId())
				.orElseThrow(() -> {
					log.error("Device not found for externalId: {}", dto.getDeviceExternalId());
					return new AmqpRejectAndDontRequeueException(
							messages.get(MessageKeys.LOCATION_DEVICE_NOT_FOUND, dto.getDeviceExternalId())
					);
				});

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
				.map(this::mapToDto)
				.orElseThrow(() -> new NotFoundException(
						messages.get(MessageKeys.LOCATION_NOT_FOUND, externalId)
				));
	}

	public List<LocationDto> getHistory(String externalId, Instant from, Instant to) {
		ensureDeviceExists(externalId);

		if (from != null && to != null && from.isAfter(to)) {
			throw new IllegalArgumentException(messages.get(MessageKeys.LOCATION_FROM_AFTER_TO));
		}

		return locationRepo.findHistory(externalId, from, to)
				.stream()
				.map(this::mapToDto)
				.toList();
	}

	public TrackSummaryDto getTrack(String externalId, Instant from, Instant to) {
		List<LocationDto> points = getHistory(externalId, from, to);

		double totalDistance = 0;
		for (int i = 1; i < points.size(); i++) {
			LocationDto prev = points.get(i - 1);
			LocationDto curr = points.get(i);
			totalDistance += GeoUtils.distanceMeters(
					prev.getLatitude(), prev.getLongitude(),
					curr.getLatitude(), curr.getLongitude()
			);
		}

		TrackSummaryDto summary = new TrackSummaryDto();
		summary.setDeviceExternalId(externalId);
		summary.setPointCount(points.size());
		summary.setTotalDistanceMeters(totalDistance);
		summary.setFrom(points.isEmpty() ? from : points.get(0).getTimestamp());
		summary.setTo(points.isEmpty() ? to : points.get(points.size() - 1).getTimestamp());
		summary.setPoints(new ArrayList<>(points));
		return summary;
	}

	public List<LocationDto> getLatestForAllDevices() {
		return locationRepo.findLatestForAllDevices()
				.stream()
				.map(this::mapToDto)
				.toList();
	}

	private void ensureDeviceExists(String externalId) {
		deviceRepo.findByExternalId(externalId)
				.orElseThrow(() -> new NotFoundException(
						messages.get(MessageKeys.DEVICE_NOT_FOUND_EXTERNAL_ID, externalId)
				));
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
