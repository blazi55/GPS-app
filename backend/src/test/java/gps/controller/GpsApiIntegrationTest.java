package gps.controller;

import gps.dto.DeviceDto;
import gps.dto.LocationDto;
import gps.enums.DeviceType;
import gps.repository.DeviceRepository;
import gps.repository.LocationRepository;
import gps.service.DeviceService;
import gps.service.LocationService;
import gps.service.producer.DeviceProducer;
import gps.service.producer.LocationProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Instant;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GpsApiIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private LocationService locationService;

	@Autowired
	private DeviceRepository deviceRepository;

	@Autowired
	private LocationRepository locationRepository;

	@MockitoBean
	private DeviceProducer deviceProducer;

	@MockitoBean
	private LocationProducer locationProducer;

	@BeforeEach
	void setUp() {
		locationRepository.deleteAll();
		deviceRepository.deleteAll();

		doAnswer(invocation -> {
			deviceService.handleIncomingDevice(invocation.getArgument(0));
			return null;
		}).when(deviceProducer).send(any(DeviceDto.class));

		doAnswer(invocation -> {
			locationService.handleIncomingLocation(invocation.getArgument(0));
			return null;
		}).when(locationProducer).send(any(LocationDto.class));
	}

	@Test
	void shouldCreateDeviceAndQueryViaHttp() throws Exception {
		mockMvc.perform(post("/device/send")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Fleet","externalId":"fleet-1","deviceType":"CAR"}
								"""))
				.andExpect(status().isAccepted());

		mockMvc.perform(get("/device/get/all"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].externalId").value("fleet-1"));

		mockMvc.perform(get("/device/get/external/{externalId}", "fleet-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Fleet"));
	}

	@Test
	void shouldIngestLocationAndReturnTrack() throws Exception {
		deviceService.handleIncomingDevice(new DeviceDto(null, "Car", DeviceType.CAR, "car-42"));

		mockMvc.perform(post("/location/send")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"deviceExternalId":"car-42","latitude":50.0,"longitude":20.0,"timestamp":"2026-01-01T10:00:00Z"}
								"""))
				.andExpect(status().isAccepted());

		mockMvc.perform(post("/location/send")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"deviceExternalId":"car-42","latitude":50.01,"longitude":20.0,"timestamp":"2026-01-01T10:10:00Z"}
								"""))
				.andExpect(status().isAccepted());

		mockMvc.perform(get("/location/{externalId}/latest", "car-42"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.latitude").value(50.01));

		mockMvc.perform(get("/location/{externalId}/history", "car-42"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));

		MvcResult track = mockMvc.perform(get("/location/{externalId}/track", "car-42"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.pointCount").value(2))
				.andReturn();

		assertTrue(track.getResponse().getContentAsString().contains("totalDistanceMeters"));
	}

	@Test
	void shouldReturnLocalizedNotFound_inPolish() throws Exception {
		mockMvc.perform(get("/device/get/{id}", 999L)
						.header("Accept-Language", "pl"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Nie znaleziono urządzenia o id: 999"));
	}

	@Test
	void shouldReturnEnglishNotFound_byDefault() throws Exception {
		mockMvc.perform(get("/device/get/{id}", 999L)
						.header("Accept-Language", "en"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Device not found for id: 999"));
	}

	@Test
	void shouldReturnLatestForAllDevices() throws Exception {
		deviceService.handleIncomingDevice(new DeviceDto(null, "A", DeviceType.PHONE, "a-1"));
		deviceService.handleIncomingDevice(new DeviceDto(null, "B", DeviceType.WATCH, "b-1"));
		locationService.handleIncomingLocation(
				new LocationDto("a-1", 1, 1, Instant.parse("2026-01-01T10:00:00Z")));
		locationService.handleIncomingLocation(
				new LocationDto("b-1", 2, 2, Instant.parse("2026-01-01T11:00:00Z")));

		mockMvc.perform(get("/location/latest/all"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));
	}
}
