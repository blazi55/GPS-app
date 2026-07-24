package gps.service;

import gps.dto.LocationDto;
import gps.dto.TrackSummaryDto;
import gps.entity.Device;
import gps.entity.Location;
import gps.exception.NotFoundException;
import gps.i18n.MessageService;
import gps.repository.DeviceRepository;
import gps.repository.LocationRepository;
import gps.support.MessageServiceTestSupport;
import org.junit.jupiter.api.BeforeEach;
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

	@Mock
	private MessageService messages;

	@InjectMocks
	private LocationService locationService;

	@BeforeEach
	void setUp() {
		MessageServiceTestSupport.stubEnglish(messages);
	}

	@Test
	void shouldSaveLocation_whenValidInput() {
		Device device = device("ext-123");
		LocationDto dto = locationDto("ext-123", 50.0, 20.0, Instant.now());

		when(deviceRepo.findByExternalId("ext-123")).thenReturn(Optional.of(device));

		locationService.handleIncomingLocation(dto);

		ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
		verify(locationRepo).save(captor.capture());

		Location saved = captor.getValue();
		assertEquals(50.0, saved.getLatitude());
		assertEquals(20.0, saved.getLongitude());
		assertEquals(device, saved.getDevice());
	}

	@Test
	void shouldAcceptBoundaryCoordinates() {
		Device device = device("ext-123");
		when(deviceRepo.findByExternalId("ext-123")).thenReturn(Optional.of(device));

		locationService.handleIncomingLocation(locationDto("ext-123", 90, 180, null));
		locationService.handleIncomingLocation(locationDto("ext-123", -90, -180, null));

		verify(locationRepo, times(2)).save(any(Location.class));
	}

	@Test
	void shouldUseCurrentTime_whenTimestampIsNull() {
		when(deviceRepo.findByExternalId("ext-123")).thenReturn(Optional.of(device("ext-123")));

		locationService.handleIncomingLocation(locationDto("ext-123", 50.0, 20.0, null));

		ArgumentCaptor<Location> captor = ArgumentCaptor.forClass(Location.class);
		verify(locationRepo).save(captor.capture());
		assertNotNull(captor.getValue().getTimestamp());
	}

	@Test
	void shouldThrowException_whenLatitudeInvalid() {
		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> locationService.handleIncomingLocation(locationDto(null, 100, 20, null)));
	}

	@Test
	void shouldThrowException_whenLongitudeInvalid() {
		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> locationService.handleIncomingLocation(locationDto(null, 50, 200, null)));
	}

	@Test
	void shouldThrowException_whenExternalIdMissing() {
		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> locationService.handleIncomingLocation(locationDto(null, 50, 20, null)));
	}

	@Test
	void shouldThrowException_whenExternalIdBlank() {
		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> locationService.handleIncomingLocation(locationDto(" ", 50, 20, null)));
	}

	@Test
	void shouldThrowException_whenDeviceNotFound() {
		when(deviceRepo.findByExternalId("ext-123")).thenReturn(Optional.empty());

		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> locationService.handleIncomingLocation(locationDto("ext-123", 50, 20, null)));
	}

	@Test
	void shouldReturnLatestLocation() {
		Location location = location(device("ext-123"), 50, 20, Instant.now());
		when(locationRepo.findLatestWithDevice("ext-123")).thenReturn(Optional.of(location));

		LocationDto result = locationService.getLatest("ext-123");

		assertEquals("ext-123", result.getDeviceExternalId());
		assertEquals(50, result.getLatitude());
		assertEquals(20, result.getLongitude());
	}

	@Test
	void shouldThrowException_whenNoLocationFound() {
		when(locationRepo.findLatestWithDevice("ext-123")).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> locationService.getLatest("ext-123"));
	}

	@Test
	void shouldReturnHistory_orderedByTimestamp() {
		Device device = device("ext-123");
		when(deviceRepo.findByExternalId("ext-123")).thenReturn(Optional.of(device));

		Location first = location(device, 10, 10, Instant.parse("2026-01-01T10:00:00Z"));
		Location second = location(device, 11, 11, Instant.parse("2026-01-01T11:00:00Z"));
		when(locationRepo.findHistory("ext-123", null, null)).thenReturn(List.of(first, second));

		List<LocationDto> history = locationService.getHistory("ext-123", null, null);

		assertEquals(2, history.size());
		assertEquals(10, history.get(0).getLatitude());
		assertEquals(11, history.get(1).getLatitude());
	}

	@Test
	void shouldThrow_whenHistoryDeviceMissing() {
		when(deviceRepo.findByExternalId("missing")).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class,
				() -> locationService.getHistory("missing", null, null));
	}

	@Test
	void shouldThrow_whenFromAfterTo() {
		when(deviceRepo.findByExternalId("ext-123")).thenReturn(Optional.of(device("ext-123")));

		Instant from = Instant.parse("2026-01-02T00:00:00Z");
		Instant to = Instant.parse("2026-01-01T00:00:00Z");

		assertThrows(IllegalArgumentException.class,
				() -> locationService.getHistory("ext-123", from, to));
	}

	@Test
	void shouldCalculateTrackDistance() {
		Device device = device("ext-123");
		when(deviceRepo.findByExternalId("ext-123")).thenReturn(Optional.of(device));

		Location first = location(device, 50.0, 20.0, Instant.parse("2026-01-01T10:00:00Z"));
		Location second = location(device, 50.001, 20.0, Instant.parse("2026-01-01T10:05:00Z"));
		when(locationRepo.findHistory("ext-123", null, null)).thenReturn(List.of(first, second));

		TrackSummaryDto track = locationService.getTrack("ext-123", null, null);

		assertEquals(2, track.getPointCount());
		assertTrue(track.getTotalDistanceMeters() > 100);
		assertTrue(track.getTotalDistanceMeters() < 150);
		assertEquals(first.getTimestamp(), track.getFrom());
		assertEquals(second.getTimestamp(), track.getTo());
	}

	@Test
	void shouldReturnEmptyTrack_whenNoPoints() {
		when(deviceRepo.findByExternalId("ext-123")).thenReturn(Optional.of(device("ext-123")));
		when(locationRepo.findHistory("ext-123", null, null)).thenReturn(List.of());

		TrackSummaryDto track = locationService.getTrack("ext-123", null, null);

		assertEquals(0, track.getPointCount());
		assertEquals(0.0, track.getTotalDistanceMeters());
		assertTrue(track.getPoints().isEmpty());
	}

	@Test
	void shouldReturnLatestForAllDevices() {
		Device a = device("a");
		Device b = device("b");
		when(locationRepo.findLatestForAllDevices()).thenReturn(List.of(
				location(a, 1, 1, Instant.now()),
				location(b, 2, 2, Instant.now())
		));

		List<LocationDto> latest = locationService.getLatestForAllDevices();

		assertEquals(2, latest.size());
		assertEquals("a", latest.get(0).getDeviceExternalId());
		assertEquals("b", latest.get(1).getDeviceExternalId());
	}

	private static Device device(String externalId) {
		Device device = new Device();
		device.setExternalId(externalId);
		return device;
	}

	private static Location location(Device device, double lat, double lon, Instant ts) {
		Location location = new Location();
		location.setDevice(device);
		location.setLatitude(lat);
		location.setLongitude(lon);
		location.setTimestamp(ts);
		return location;
	}

	private static LocationDto locationDto(String externalId, double lat, double lon, Instant ts) {
		LocationDto dto = new LocationDto();
		dto.setDeviceExternalId(externalId);
		dto.setLatitude(lat);
		dto.setLongitude(lon);
		dto.setTimestamp(ts);
		return dto;
	}
}
