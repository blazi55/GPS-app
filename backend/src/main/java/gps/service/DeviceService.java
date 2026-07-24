package gps.service;

import gps.dto.DeviceDto;
import gps.dto.SendDeviceDto;
import gps.entity.Device;
import gps.exception.NotFoundException;
import gps.i18n.MessageKeys;
import gps.i18n.MessageService;
import gps.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

	private final DeviceRepository deviceRepository;
	private final MessageService messages;

	public DeviceDto getDevice(final Long id) {
		return deviceRepository.findById(id)
				.map(this::mapToDto)
				.orElseThrow(() -> {
					log.error("Device not found for id: {}", id);
					return new NotFoundException(messages.get(MessageKeys.DEVICE_NOT_FOUND_ID, id));
				});
	}

	public DeviceDto getDeviceByExternalId(final String externalId) {
		return deviceRepository.findByExternalId(externalId)
				.map(this::mapToDto)
				.orElseThrow(() -> {
					log.error("Device not found for externalId: {}", externalId);
					return new NotFoundException(
							messages.get(MessageKeys.DEVICE_NOT_FOUND_EXTERNAL_ID, externalId)
					);
				});
	}

	@Transactional
	public void handleIncomingDevice(final DeviceDto dto) {
		if (dto.getExternalId() == null || dto.getExternalId().isBlank()) {
			log.error("ExternalId is required");
			throw new AmqpRejectAndDontRequeueException(
					messages.get(MessageKeys.DEVICE_EXTERNAL_ID_REQUIRED)
			);
		}

		if (dto.getName() == null || dto.getName().isBlank()) {
			log.error("Device name cannot be empty");
			throw new AmqpRejectAndDontRequeueException(
					messages.get(MessageKeys.DEVICE_NAME_REQUIRED)
			);
		}

		final Device device = deviceRepository
				.findByExternalId(dto.getExternalId())
				.orElseGet(Device::new);

		device.setExternalId(dto.getExternalId());
		device.setName(dto.getName());
		device.setDeviceType(dto.getDeviceType());

		deviceRepository.save(device);
	}

	public List<DeviceDto> getAllDevices() {
		return deviceRepository.findAll()
				.stream()
				.map(this::mapToDto)
				.toList();
	}

	private DeviceDto mapToDto(final Device device) {
		final DeviceDto dto = new DeviceDto();
		dto.setId(device.getId());
		dto.setName(device.getName());
		dto.setExternalId(device.getExternalId());
		dto.setDeviceType(device.getDeviceType());
		return dto;
	}

	public DeviceDto mapSendToDto(final SendDeviceDto sendDeviceDto) {
		final DeviceDto dto = new DeviceDto();
		dto.setName(sendDeviceDto.getName());
		dto.setExternalId(sendDeviceDto.getExternalId());
		dto.setDeviceType(sendDeviceDto.getDeviceType());
		return dto;
	}
}
