package gps.controller;

import gps.dto.DeviceDto;
import gps.service.DeviceService;
import gps.service.producer.DeviceProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/device")
@RequiredArgsConstructor
public class DeviceController {

	private final DeviceProducer producer;
	private final DeviceService deviceService;

	@PostMapping("/send")
	public void sendDevice(@RequestBody DeviceDto dto) {
		producer.send(dto);
	}


	@GetMapping("get/{id}")
	public ResponseEntity<DeviceDto> getDevice(@PathVariable final Long id) {
		return ResponseEntity.ok(deviceService.getDevice(id));
	}

	@GetMapping
	public List<DeviceDto> getAll() {
		return deviceService.getAllDevices();
	}
}
