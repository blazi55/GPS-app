package gps.controller;

import gps.dto.DeviceDto;
import gps.dto.SendDeviceDto;
import gps.service.DeviceService;
import gps.service.producer.DeviceProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController {

	private final DeviceProducer producer;
	private final DeviceService deviceService;

	@PostMapping("/send")
	public void sendDevice(@RequestBody SendDeviceDto dto) {
		log.info("Data's to create Device is sent {}", dto);
		final DeviceDto deviceDto = deviceService.mapSendToDto(dto);
		log.info("Device is send {}", deviceDto);
		producer.send(deviceDto);
	}


	@GetMapping("get/{id}")
	public ResponseEntity<DeviceDto> getDevice(@PathVariable final Long id) {
		log.info("Get Device by id {}", id);
		return ResponseEntity.ok(deviceService.getDevice(id));
	}

	@GetMapping("get/all")
	public List<DeviceDto> getAll() {
		return deviceService.getAllDevices();
	}
}
