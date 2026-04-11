package gps.controller;

import gps.dto.LocationDto;
import gps.service.LocationService;
import gps.service.producer.LocationProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {

	private final LocationProducer producer;
	private final LocationService locationService;

	@PostMapping("/send")
	public void sendLocation(@RequestBody final LocationDto dto) {
		producer.send(dto);
	}

	@GetMapping("/{externalId}/latest")
	public ResponseEntity<LocationDto> getLatest(@PathVariable final String externalId) {
		return ResponseEntity.ok(locationService.getLatest(externalId));
	}
}
