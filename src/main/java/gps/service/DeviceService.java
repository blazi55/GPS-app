package gps.service;

import gps.dto.DeviceDto;
import gps.entity.Device;
import gps.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {

	private final DeviceRepository deviceRepository;

	public DeviceDto getDevice(final Long id) {
		final Device device = deviceRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Device not found"));
		return mapToDto(device);
	}

	@Transactional
	public void handleIncomingDevice(final DeviceDto dto) {
		if (dto.getExternalId() == null || dto.getExternalId().isBlank()) {
			throw new IllegalArgumentException("ExternalId is required");
		}

		if (dto.getName() == null || dto.getName().isBlank()) {
			throw new IllegalArgumentException("Device name cannot be empty");
		}
		final Device device = deviceRepository
				.findByExternalId(dto.getExternalId())
				.orElseGet(Device::new);

		device.setExternalId(dto.getExternalId());
		device.setName(dto.getName());

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
		return dto;
	}
}
