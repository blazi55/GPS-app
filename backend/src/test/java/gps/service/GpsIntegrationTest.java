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

	@Test
	void shouldReturnHistoryAndTrackSummary() {
		DeviceDto deviceDto = new DeviceDto();
		deviceDto.setName("Device A");
		deviceDto.setExternalId("ext-track");
		deviceDto.setDeviceType(DeviceType.CAR);

		deviceService.handleIncomingDevice(deviceDto);

		LocationDto p1 = new LocationDto();
		p1.setDeviceExternalId("ext-track");
		p1.setLatitude(50.0);
		p1.setLongitude(20.0);
		p1.setTimestamp(Instant.parse("2026-01-01T10:00:00Z"));

		LocationDto p2 = new LocationDto();
		p2.setDeviceExternalId("ext-track");
		p2.setLatitude(50.01);
		p2.setLongitude(20.0);
		p2.setTimestamp(Instant.parse("2026-01-01T10:10:00Z"));

		locationService.handleIncomingLocation(p1);
		locationService.handleIncomingLocation(p2);

		var history = locationService.getHistory("ext-track", null, null);
		assertEquals(2, history.size());

		var track = locationService.getTrack("ext-track", null, null);
		assertEquals(2, track.getPointCount());
		assertTrue(track.getTotalDistanceMeters() > 1000);

		var filtered = locationService.getHistory(
				"ext-track",
				Instant.parse("2026-01-01T10:05:00Z"),
				null
		);
		assertEquals(1, filtered.size());
		assertEquals(50.01, filtered.get(0).getLatitude());
	}

	@Test
	void shouldReturnLatestForAllDevices() {
		DeviceDto a = new DeviceDto();
		a.setName("A");
		a.setExternalId("dev-a");
		a.setDeviceType(DeviceType.PHONE);

		DeviceDto b = new DeviceDto();
		b.setName("B");
		b.setExternalId("dev-b");
		b.setDeviceType(DeviceType.WATCH);

		deviceService.handleIncomingDevice(a);
		deviceService.handleIncomingDevice(b);

		LocationDto locA = new LocationDto();
		locA.setDeviceExternalId("dev-a");
		locA.setLatitude(1);
		locA.setLongitude(1);
		locA.setTimestamp(Instant.now());

		LocationDto locB = new LocationDto();
		locB.setDeviceExternalId("dev-b");
		locB.setLatitude(2);
		locB.setLongitude(2);
		locB.setTimestamp(Instant.now());

		locationService.handleIncomingLocation(locA);
		locationService.handleIncomingLocation(locB);

		var latest = locationService.getLatestForAllDevices();
		assertEquals(2, latest.size());
	}

	@Test
	void shouldGetDeviceByExternalId() {
		DeviceDto dto = new DeviceDto();
		dto.setName("Tablet");
		dto.setExternalId("tab-1");
		dto.setDeviceType(DeviceType.TABLET);
		deviceService.handleIncomingDevice(dto);

		var found = deviceService.getDeviceByExternalId("tab-1");
		assertEquals("Tablet", found.getName());
		assertEquals(DeviceType.TABLET, found.getDeviceType());
	}

	@Test
	void shouldRejectInvalidLongitude() {
		DeviceDto deviceDto = new DeviceDto();
		deviceDto.setName("Device A");
		deviceDto.setExternalId("ext-lon");
		deviceService.handleIncomingDevice(deviceDto);

		LocationDto dto = new LocationDto();
		dto.setDeviceExternalId("ext-lon");
		dto.setLatitude(50);
		dto.setLongitude(200);

		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> locationService.handleIncomingLocation(dto));
	}

	@Test
	void shouldReturnEmptyHistory_whenDeviceHasNoPoints() {
		DeviceDto deviceDto = new DeviceDto();
		deviceDto.setName("Empty");
		deviceDto.setExternalId("empty-1");
		deviceService.handleIncomingDevice(deviceDto);

		assertTrue(locationService.getHistory("empty-1", null, null).isEmpty());
		assertEquals(0, locationService.getTrack("empty-1", null, null).getPointCount());
	}

	@Test
	void shouldFilterHistoryByToTimestamp() {
		DeviceDto deviceDto = new DeviceDto();
		deviceDto.setName("Filter");
		deviceDto.setExternalId("filter-1");
		deviceService.handleIncomingDevice(deviceDto);

		LocationDto p1 = new LocationDto();
		p1.setDeviceExternalId("filter-1");
		p1.setLatitude(1);
		p1.setLongitude(1);
		p1.setTimestamp(Instant.parse("2026-01-01T10:00:00Z"));

		LocationDto p2 = new LocationDto();
		p2.setDeviceExternalId("filter-1");
		p2.setLatitude(2);
		p2.setLongitude(2);
		p2.setTimestamp(Instant.parse("2026-01-01T12:00:00Z"));

		locationService.handleIncomingLocation(p1);
		locationService.handleIncomingLocation(p2);

		var filtered = locationService.getHistory(
				"filter-1",
				null,
				Instant.parse("2026-01-01T10:30:00Z")
		);
		assertEquals(1, filtered.size());
		assertEquals(1, filtered.get(0).getLatitude());
	}
}