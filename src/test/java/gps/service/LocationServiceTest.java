package gps.service;

import gps.dto.LocationDto;
import gps.entity.Device;
import gps.entity.Location;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
	void shouldThrowException_whenLongitudeInvalid() {
		LocationDto dto = new LocationDto();
		dto.setLongitude(200);

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
	void shouldThrowException_whenDeviceNotFound() {
		LocationDto dto = new LocationDto();
		dto.setDeviceExternalId("ext-123");
		dto.setLatitude(50);
		dto.setLongitude(20);

		when(deviceRepo.findByExternalId("ext-123"))
				.thenReturn(Optional.empty());

		assertThrows(RuntimeException.class,
				() -> locationService.handleIncomingLocation(dto));
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
				.thenReturn(List.of(location));

		LocationDto result = locationService.getLatest("ext-123");

		assertEquals("ext-123", result.getDeviceExternalId());
		assertEquals(50, result.getLatitude());
		assertEquals(20, result.getLongitude());
	}

	@Test
	void shouldThrowException_whenNoLocationFound() {
		when(locationRepo.findLatestWithDevice("ext-123"))
				.thenReturn(List.of());

		assertThrows(RuntimeException.class,
				() -> locationService.getLatest("ext-123"));
	}
}