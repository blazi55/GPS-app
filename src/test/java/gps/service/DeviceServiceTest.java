package gps.service;

import gps.controller.SendDeviceDto;
import gps.dto.DeviceDto;
import gps.entity.Device;
import gps.repository.DeviceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

	@Mock
	private DeviceRepository deviceRepository;

	@InjectMocks
	private DeviceService deviceService;

	@Test
	void shouldReturnDeviceDto_whenDeviceExists() {
		Device device = new Device();
		device.setId(1L);
		device.setName("Device A");
		device.setExternalId("ext-123");

		when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

		DeviceDto result = deviceService.getDevice(1L);

		assertEquals(1L, result.getId());
		assertEquals("Device A", result.getName());
		assertEquals("ext-123", result.getExternalId());
	}

	@Test
	void shouldThrowException_whenDeviceNotFound() {
		when(deviceRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> deviceService.getDevice(1L));
	}


	@Test
	void shouldSaveNewDevice_whenNotExists() {
		DeviceDto dto = new DeviceDto();
		dto.setName("Device A");
		dto.setExternalId("ext-123");

		when(deviceRepository.findByExternalId("ext-123"))
				.thenReturn(Optional.empty());

		deviceService.handleIncomingDevice(dto);

		verify(deviceRepository).save(any(Device.class));
	}

	@Test
	void shouldUpdateExistingDevice_whenExists() {
		Device existing = new Device();
		existing.setId(1L);
		existing.setExternalId("ext-123");

		DeviceDto dto = new DeviceDto();
		dto.setName("Updated Name");
		dto.setExternalId("ext-123");

		when(deviceRepository.findByExternalId("ext-123"))
				.thenReturn(Optional.of(existing));

		deviceService.handleIncomingDevice(dto);

		assertEquals("Updated Name", existing.getName());
		verify(deviceRepository).save(existing);
	}

	@Test
	void shouldThrowException_whenExternalIdIsNull() {
		DeviceDto dto = new DeviceDto();
		dto.setName("Device");

		assertThrows(IllegalArgumentException.class,
				() -> deviceService.handleIncomingDevice(dto));
	}

	@Test
	void shouldThrowException_whenNameIsBlank() {
		DeviceDto dto = new DeviceDto();
		dto.setExternalId("ext-123");
		dto.setName(" ");

		assertThrows(IllegalArgumentException.class,
				() -> deviceService.handleIncomingDevice(dto));
	}

	@Test
	void shouldReturnListOfDtos() {
		Device device = new Device();
		device.setId(1L);
		device.setName("Device A");
		device.setExternalId("ext-123");

		when(deviceRepository.findAll()).thenReturn(List.of(device));

		List<DeviceDto> result = deviceService.getAllDevices();

		assertEquals(1, result.size());
		assertEquals("Device A", result.get(0).getName());
	}

	@Test
	void shouldMapSendDeviceDtoToDeviceDto() {
		SendDeviceDto send = new SendDeviceDto();
		send.setName("Device A");
		send.setExternalId("ext-123");

		DeviceDto result = deviceService.mapSendToDto(send);

		assertEquals("Device A", result.getName());
		assertEquals("ext-123", result.getExternalId());
	}
}