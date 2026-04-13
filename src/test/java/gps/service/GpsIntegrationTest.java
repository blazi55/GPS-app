package gps.service;

import gps.dto.DeviceDto;
import gps.dto.LocationDto;
import gps.entity.Device;
import gps.enums.DeviceType;
import gps.exception.NotFoundException;
import gps.repository.DeviceRepository;
import gps.repository.LocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class GpsIntegrationTest {

	@Autowired
	private LocationService locationService;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private DeviceRepository deviceRepository;

	@Autowired
	private LocationRepository locationRepository;

	@BeforeEach
	void cleanDb() {
		locationRepository.deleteAll();
		deviceRepository.deleteAll();
	}

	@Test
	void shouldHandleFullFlow_deviceAndLocation() {
		DeviceDto deviceDto = new DeviceDto();
		deviceDto.setName("Device A");
		deviceDto.setExternalId("ext-123");
		deviceDto.setDeviceType(DeviceType.PHONE);

		deviceService.handleIncomingDevice(deviceDto);

		Device device = deviceRepository.findByExternalId("ext-123")
				.orElseThrow(() -> new NotFoundException("Device should exist"));

		assertNotNull(device.getId());
		assertEquals(DeviceType.PHONE, device.getDeviceType());

		LocationDto locationDto = new LocationDto();
		locationDto.setDeviceExternalId("ext-123");
		locationDto.setLatitude(50.0);
		locationDto.setLongitude(20.0);
		locationDto.setTimestamp(Instant.now());

		locationService.handleIncomingLocation(locationDto);

		LocationDto result = locationService.getLatest("ext-123");

		assertEquals("ext-123", result.getDeviceExternalId());
		assertEquals(50.0, result.getLatitude());
		assertEquals(20.0, result.getLongitude());
		assertEquals(1, locationRepository.count());
	}

	@Test
	void shouldThrow_whenSavingLocationForNonExistingDevice() {
		LocationDto dto = new LocationDto();
		dto.setDeviceExternalId("not-exists");
		dto.setLatitude(50);
		dto.setLongitude(20);

		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> locationService.handleIncomingLocation(dto));
	}

	@Test
	void shouldThrow_whenLatitudeInvalid() {
		DeviceDto deviceDto = new DeviceDto();
		deviceDto.setName("Device A");
		deviceDto.setExternalId("ext-123");

		deviceService.handleIncomingDevice(deviceDto);

		LocationDto dto = new LocationDto();
		dto.setDeviceExternalId("ext-123");
		dto.setLatitude(100);
		dto.setLongitude(20);

		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> locationService.handleIncomingLocation(dto));
	}

	@Test
	void shouldThrowNotFound_whenNoLocations() {
		DeviceDto deviceDto = new DeviceDto();
		deviceDto.setName("Device A");
		deviceDto.setExternalId("ext-123");

		deviceService.handleIncomingDevice(deviceDto);

		assertThrows(NotFoundException.class,
				() -> locationService.getLatest("ext-123"));
	}

	@Test
	void shouldUpdateDevice_whenSameExternalId() {
		DeviceDto dto = new DeviceDto();
		dto.setName("Device A");
		dto.setExternalId("ext-123");
		dto.setDeviceType(DeviceType.PHONE);

		deviceService.handleIncomingDevice(dto);

		dto.setName("Updated");
		dto.setDeviceType(DeviceType.CAR);

		deviceService.handleIncomingDevice(dto);

		Device device = deviceRepository.findByExternalId("ext-123").orElseThrow();

		assertEquals("Updated", device.getName());
		assertEquals(DeviceType.CAR, device.getDeviceType());
	}

	@Test
	void shouldReturnLatestLocation_whenMultipleExist() {
		DeviceDto deviceDto = new DeviceDto();
		deviceDto.setName("Device A");
		deviceDto.setExternalId("ext-123");

		deviceService.handleIncomingDevice(deviceDto);

		LocationDto older = new LocationDto();
		older.setDeviceExternalId("ext-123");
		older.setLatitude(10);
		older.setLongitude(10);
		older.setTimestamp(Instant.now().minusSeconds(60));

		LocationDto newer = new LocationDto();
		newer.setDeviceExternalId("ext-123");
		newer.setLatitude(50);
		newer.setLongitude(20);
		newer.setTimestamp(Instant.now());

		locationService.handleIncomingLocation(older);
		locationService.handleIncomingLocation(newer);

		LocationDto result = locationService.getLatest("ext-123");

		assertEquals(50, result.getLatitude());
		assertEquals(20, result.getLongitude());
	}

	@Test
	void shouldSetTimestamp_whenNull() {
		DeviceDto deviceDto = new DeviceDto();
		deviceDto.setName("Device A");
		deviceDto.setExternalId("ext-123");

		deviceService.handleIncomingDevice(deviceDto);

		LocationDto dto = new LocationDto();
		dto.setDeviceExternalId("ext-123");
		dto.setLatitude(50);
		dto.setLongitude(20);

		locationService.handleIncomingLocation(dto);

		var saved = locationRepository.findAll().get(0);

		assertNotNull(saved.getTimestamp());
	}
}