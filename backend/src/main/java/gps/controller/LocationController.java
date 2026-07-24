package gps.controller;

import gps.dto.LocationDto;
import gps.dto.TrackSummaryDto;
import gps.service.LocationService;
import gps.service.producer.LocationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class LocationController {

	private final LocationProducer producer;
	private final LocationService locationService;

	@PostMapping("/send")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void sendLocation(@RequestBody final LocationDto dto) {
		log.info("Location is send {}", dto);
		producer.send(dto);
	}

	@GetMapping("/{externalId}/latest")
	public ResponseEntity<LocationDto> getLatest(@PathVariable final String externalId) {
		return ResponseEntity.ok(locationService.getLatest(externalId));
	}

	@GetMapping("/{externalId}/history")
	public ResponseEntity<List<LocationDto>> getHistory(
			@PathVariable final String externalId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
	) {
		return ResponseEntity.ok(locationService.getHistory(externalId, from, to));
	}

	@GetMapping("/{externalId}/track")
	public ResponseEntity<TrackSummaryDto> getTrack(
			@PathVariable final String externalId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
	) {
		return ResponseEntity.ok(locationService.getTrack(externalId, from, to));
	}

	@GetMapping("/latest/all")
	public ResponseEntity<List<LocationDto>> getLatestForAllDevices() {
		return ResponseEntity.ok(locationService.getLatestForAllDevices());
	}
}
