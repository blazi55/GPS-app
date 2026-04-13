package gps.service;

import gps.controller.SendDeviceDto;
import gps.dto.DeviceDto;
import gps.entity.Device;
import gps.enums.DeviceType;
import gps.exception.NotFoundException;
import gps.repository.DeviceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
		device.setDeviceType(DeviceType.PHONE);

		when(deviceRepository.findById(1L)).thenReturn(Optional.of(device));

		DeviceDto result = deviceService.getDevice(1L);

		assertEquals(1L, result.getId());
		assertEquals("Device A", result.getName());
		assertEquals("ext-123", result.getExternalId());
		assertEquals(DeviceType.PHONE, result.getDeviceType());
	}

	@Test
	void shouldThrowException_whenDeviceNotFound() {
		when(deviceRepository.findById(1L)).thenReturn(Optional.empty());

		NotFoundException ex = assertThrows(
				NotFoundException.class,
				() -> deviceService.getDevice(1L)
		);

		assertTrue(ex.getMessage().contains("Device not found for id: 1"));
	}

	@Test
	void shouldSaveNewDevice_whenNotExists() {
		DeviceDto dto = new DeviceDto();
		dto.setName("Device A");
		dto.setExternalId("ext-123");
		dto.setDeviceType(DeviceType.PHONE);

		when(deviceRepository.findByExternalId("ext-123"))
				.thenReturn(Optional.empty());

		deviceService.handleIncomingDevice(dto);

		ArgumentCaptor<Device> captor = ArgumentCaptor.forClass(Device.class);
		verify(deviceRepository).save(captor.capture());

		Device saved = captor.getValue();
		assertEquals("ext-123", saved.getExternalId());
		assertEquals("Device A", saved.getName());
		assertEquals(DeviceType.PHONE, saved.getDeviceType());
	}

	@Test
	void shouldUpdateExistingDevice_whenExists() {
		Device existing = new Device();
		existing.setId(1L);
		existing.setExternalId("ext-123");
		existing.setDeviceType(DeviceType.CAR);

		DeviceDto dto = new DeviceDto();
		dto.setName("Updated Name");
		dto.setExternalId("ext-123");
		dto.setDeviceType(DeviceType.PHONE);

		when(deviceRepository.findByExternalId("ext-123"))
				.thenReturn(Optional.of(existing));

		deviceService.handleIncomingDevice(dto);

		assertEquals("Updated Name", existing.getName());
		assertEquals(DeviceType.PHONE, existing.getDeviceType());

		verify(deviceRepository).save(existing);
	}

	@Test
	void shouldNotCreateNewDevice_whenUpdating() {
		Device existing = new Device();
		existing.setId(1L);
		existing.setExternalId("ext-123");

		when(deviceRepository.findByExternalId("ext-123"))
				.thenReturn(Optional.of(existing));

		DeviceDto dto = new DeviceDto();
		dto.setExternalId("ext-123");
		dto.setName("Updated");

		deviceService.handleIncomingDevice(dto);

		verify(deviceRepository, times(1)).save(existing);
	}

	@Test
	void shouldThrowException_whenExternalIdIsNull() {
		DeviceDto dto = new DeviceDto();
		dto.setName("Device");

		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> deviceService.handleIncomingDevice(dto));
	}

	@Test
	void shouldThrowException_whenExternalIdIsBlank() {
		DeviceDto dto = new DeviceDto();
		dto.setExternalId(" ");
		dto.setName("Device");

		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> deviceService.handleIncomingDevice(dto));
	}

	@Test
	void shouldThrowException_whenNameIsBlank() {
		DeviceDto dto = new DeviceDto();
		dto.setExternalId("ext-123");
		dto.setName(" ");

		assertThrows(AmqpRejectAndDontRequeueException.class,
				() -> deviceService.handleIncomingDevice(dto));
	}

	@Test
	void shouldAllowNullDeviceType() {
		DeviceDto dto = new DeviceDto();
		dto.setExternalId("ext-123");
		dto.setName("Device A");

		when(deviceRepository.findByExternalId("ext-123"))
				.thenReturn(Optional.empty());

		deviceService.handleIncomingDevice(dto);

		verify(deviceRepository).save(any(Device.class));
	}

	@Test
	void shouldReturnEmptyList_whenNoDevices() {
		when(deviceRepository.findAll()).thenReturn(List.of());

		List<DeviceDto> result = deviceService.getAllDevices();

		assertTrue(result.isEmpty());
	}

	@Test
	void shouldReturnListOfDtos() {
		Device device = new Device();
		device.setId(1L);
		device.setName("Device A");
		device.setExternalId("ext-123");
		device.setDeviceType(DeviceType.DRONE);

		when(deviceRepository.findAll()).thenReturn(List.of(device));

		List<DeviceDto> result = deviceService.getAllDevices();

		assertEquals(1, result.size());
		assertEquals("Device A", result.get(0).getName());
		assertEquals(DeviceType.DRONE, result.get(0).getDeviceType());
	}

	@Test
	void shouldMapSendDeviceDtoToDeviceDto() {
		SendDeviceDto send = new SendDeviceDto();
		send.setName("Device A");
		send.setExternalId("ext-123");
		send.setDeviceType(DeviceType.TABLET);

		DeviceDto result = deviceService.mapSendToDto(send);

		assertEquals("Device A", result.getName());
		assertEquals("ext-123", result.getExternalId());
		assertEquals(DeviceType.TABLET, result.getDeviceType());
	}

	@Test
	void shouldMapSendDeviceDto_whenDeviceTypeNull() {
		SendDeviceDto send = new SendDeviceDto();
		send.setName("Device A");
		send.setExternalId("ext-123");

		DeviceDto result = deviceService.mapSendToDto(send);

		assertEquals("Device A", result.getName());
		assertEquals("ext-123", result.getExternalId());
		assertNull(result.getDeviceType());
	}
}