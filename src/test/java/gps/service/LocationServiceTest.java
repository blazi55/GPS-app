package gps.service;

import gps.dto.LocationDto;
import gps.entity.Device;
import gps.entity.Location;
import gps.exception.NotFoundException;
import gps.repository.DeviceRepository;
import gps.repository.LocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

	@Mock
	private DeviceRepository deviceRepo;

	@Mock
	private LocationRepository locationRepo;

	@InjectMocks
	private LocationService locationService;

	@Test
	void shouldSaveLocation_whenValidInput() {
		Device device = new Device();
		device.setExternalId("ext-123");

		LocationDto dto = new LocationDto();
		dto.setDeviceExternalId("ext-123");
		dto.setLatitude(50.0);
		dto.setLongitude(20.0);
		dto.setTimestamp(Instant.now());

		when(deviceRepo.findByExternalId("ext-123"))
				.thenReturn(Optional.of(device));

		locationService.handleIncomingLocation(dto);

		ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
		verify(locationRepo).save(captor.capture());

		Location saved = captor.getValue();
		assertEquals(50.0, saved.getLatitude());
		assertEquals(20.0, saved.getLongitude());
		assertEquals(device, saved.getDevice());
	}

	@Test
	void shouldUseCurrentTime_whenTimestampIsNull() {
		Device device = new Device();
		device.setExternalId("ext-123");

		LocationDto dto = new LocationDto();
		dto.setDeviceExternalId("ext-123");
		dto.setLatitude(50.0);
		dto.setLongitude(20.0);

		when(deviceRepo.findByExternalId("ext-123"))
				.thenReturn(Optional.of(device));

		locationService.handleIncomingLocation(dto);

		ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
		verify(locationRepo).save(captor.capture());

		assertNotNull(captor.getValue().getTimestamp());
	}

	@Test
	void shouldThrowException_whenLatitudeInvalid() {
		LocationDto dto = new LocationDto();
		dto.setLatitude(100);

		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> locationService.handleIncomingLocation(dto));
	}

	@Test
	void shouldThrowException_whenLatitudeTooLow() {
		LocationDto dto = new LocationDto();
		dto.setLatitude(-100);

		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> locationService.handleIncomingLocation(dto));
	}

	@Test
	void shouldThrowException_whenLongitudeInvalid() {
		LocationDto dto = new LocationDto();
		dto.setLongitude(200);

		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> locationService.handleIncomingLocation(dto));
	}

	@Test
	void shouldThrowException_whenLongitudeTooLow() {
		LocationDto dto = new LocationDto();
		dto.setLongitude(-200);

		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> locationService.handleIncomingLocation(dto));
	}

	@Test
	void shouldThrowException_whenExternalIdMissing() {
		LocationDto dto = new LocationDto();
		dto.setLatitude(50);
		dto.setLongitude(20);

		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> locationService.handleIncomingLocation(dto));
	}

	@Test
	void shouldThrowException_whenExternalIdBlank() {
		LocationDto dto = new LocationDto();
		dto.setDeviceExternalId(" ");
		dto.setLatitude(50);
		dto.setLongitude(20);

		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> locationService.handleIncomingLocation(dto));
	}

	@Test
	void shouldThrowException_whenDeviceNotFound() {
		LocationDto dto = new LocationDto();
		dto.setDeviceExternalId("ext-123");
		dto.setLatitude(50);
		dto.setLongitude(20);

		when(deviceRepo.findByExternalId("ext-123"))
				.thenReturn(Optional.empty());

		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> locationService.handleIncomingLocation(dto));
	}

	@Test
	void shouldCallRepositoryOnce_whenSaving() {
		Device device = new Device();
		device.setExternalId("ext-123");

		LocationDto dto = new LocationDto();
		dto.setDeviceExternalId("ext-123");
		dto.setLatitude(50);
		dto.setLongitude(20);

		when(deviceRepo.findByExternalId("ext-123"))
				.thenReturn(Optional.of(device));

		locationService.handleIncomingLocation(dto);

		verify(locationRepo, times(1)).save(any(Location.class));
	}

	@Test
	void shouldReturnLatestLocation() {
		Device device = new Device();
		device.setExternalId("ext-123");

		Location location = new Location();
		location.setDevice(device);
		location.setLatitude(50);
		location.setLongitude(20);
		location.setTimestamp(Instant.now());

		when(locationRepo.findLatestWithDevice("ext-123"))
				.thenReturn(Optional.of(location));

		LocationDto result = locationService.getLatest("ext-123");

		assertEquals("ext-123", result.getDeviceExternalId());
		assertEquals(50, result.getLatitude());
		assertEquals(20, result.getLongitude());
	}

	@Test
	void shouldMapAllFieldsCorrectly() {
		Device device = new Device();
		device.setExternalId("ext-123");

		Instant now = Instant.now();

		Location location = new Location();
		location.setDevice(device);
		location.setLatitude(10);
		location.setLongitude(15);
		location.setTimestamp(now);

		when(locationRepo.findLatestWithDevice("ext-123"))
				.thenReturn(Optional.of(location));

		LocationDto result = locationService.getLatest("ext-123");

		assertEquals("ext-123", result.getDeviceExternalId());
		assertEquals(10, result.getLatitude());
		assertEquals(15, result.getLongitude());
		assertEquals(now, result.getTimestamp());
	}

	@Test
	void shouldThrowException_whenNoLocationFound() {
		when(locationRepo.findLatestWithDevice("ext-123"))
				.thenReturn(Optional.empty());

		assertThrows(NotFoundException.class,
				() -> locationService.getLatest("ext-123"));
	}

	@Test
	void shouldReturnHistory_orderedByTimestamp() {
		Device device = new Device();
		device.setExternalId("ext-123");

		when(deviceRepo.findByExternalId("ext-123")).thenReturn(Optional.of(device));

		Location first = new Location();
		first.setDevice(device);
		first.setLatitude(10);
		first.setLongitude(10);
		first.setTimestamp(Instant.parse("2026-01-01T10:00:00Z"));

		Location second = new Location();
		second.setDevice(device);
		second.setLatitude(11);
		second.setLongitude(11);
		second.setTimestamp(Instant.parse("2026-01-01T11:00:00Z"));

		when(locationRepo.findHistory("ext-123", null, null))
				.thenReturn(List.of(first, second));

		List<LocationDto> history = locationService.getHistory("ext-123", null, null);

		assertEquals(2, history.size());
		assertEquals(10, history.get(0).getLatitude());
		assertEquals(11, history.get(1).getLatitude());
	}

	@Test
	void shouldCalculateTrackDistance() {
		Device device = new Device();
		device.setExternalId("ext-123");

		when(deviceRepo.findByExternalId("ext-123")).thenReturn(Optional.of(device));

		Location first = new Location();
		first.setDevice(device);
		first.setLatitude(50.0);
		first.setLongitude(20.0);
		first.setTimestamp(Instant.parse("2026-01-01T10:00:00Z"));

		Location second = new Location();
		second.setDevice(device);
		second.setLatitude(50.001);
		second.setLongitude(20.0);
		second.setTimestamp(Instant.parse("2026-01-01T10:05:00Z"));

		when(locationRepo.findHistory("ext-123", null, null))
				.thenReturn(List.of(first, second));

		var track = locationService.getTrack("ext-123", null, null);

		assertEquals(2, track.getPointCount());
		assertTrue(track.getTotalDistanceMeters() > 100);
		assertTrue(track.getTotalDistanceMeters() < 150);
	}

	@Test
	void shouldThrow_whenFromAfterTo() {
		Device device = new Device();
		device.setExternalId("ext-123");
		when(deviceRepo.findByExternalId("ext-123")).thenReturn(Optional.of(device));

		Instant from = Instant.parse("2026-01-02T00:00:00Z");
		Instant to = Instant.parse("2026-01-01T00:00:00Z");

		assertThrows(IllegalArgumentException.class,
				() -> locationService.getHistory("ext-123", from, to));
	}
}