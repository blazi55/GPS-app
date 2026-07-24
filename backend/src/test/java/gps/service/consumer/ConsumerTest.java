package gps.service.consumer;

import gps.dto.DeviceDto;
import gps.dto.LocationDto;
import gps.enums.DeviceType;
import gps.service.DeviceService;
import gps.service.LocationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConsumerTest {

	@Mock
	private DeviceService deviceService;

	@Mock
	private LocationService locationService;

	@InjectMocks
	private DeviceConsumer deviceConsumer;

	@InjectMocks
	private LocationConsumer locationConsumer;

	@Test
	void shouldDelegateDeviceToService() {
		DeviceDto dto = new DeviceDto(null, "Car", DeviceType.CAR, "car-1");

		deviceConsumer.consume(dto);

		verify(deviceService).handleIncomingDevice(dto);
	}

	@Test
	void shouldDelegateLocationToService() {
		LocationDto dto = new LocationDto("car-1", 52.0, 21.0, Instant.now());

		locationConsumer.consume(dto);

		verify(locationService).handleIncomingLocation(dto);
	}
}
