package gps.service;

import gps.dto.DeviceDto;
import gps.dto.LocationDto;
import gps.entity.Device;
import gps.enums.DeviceType;
import gps.repository.DeviceRepository;
import gps.repository.LocationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class GpsIntegrationTest {

	@Autowired
	private LocationService locationService;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private DeviceRepository deviceRepository;

	@Autowired
	private LocationRepository locationRepository;

	@Test
	void shouldHandleFullFlow_deviceAndLocation() {
		DeviceDto deviceDto = new DeviceDto();
		deviceDto.setName("Device A");
		deviceDto.setExternalId("ext-123");
		deviceDto.setDeviceType(DeviceType.PHONE);

		deviceService.handleIncomingDevice(deviceDto);

		Device device = deviceRepository.findByExternalId("ext-123").orElseThrow();
		assertNotNull(device.getId());
		assertEquals(DeviceType.PHONE, device.getDeviceType());

		LocationDto locationDto = new LocationDto();
		locationDto.setDeviceExternalId("ext-123");
		locationDto.setLatitude(50.0);
		locationDto.setLongitude(20.0);
		locationDto.setTimestamp(Instant.now());

		locationService.handleIncomingLocation(locationDto);

		var result = locationService.getLatest("ext-123");
		assertEquals("ext-123", result.getDeviceExternalId());
		assertEquals(50.0, result.getLatitude());
		assertEquals(20.0, result.getLongitude());
		assertEquals(1, locationRepository.count());
	}
}