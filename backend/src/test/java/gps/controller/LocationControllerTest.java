package gps.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gps.dto.LocationDto;
import gps.dto.TrackSummaryDto;
import gps.exception.GlobalExceptionHandler;
import gps.exception.NotFoundException;
import gps.i18n.MessageService;
import gps.service.LocationService;
import gps.service.producer.LocationProducer;
import gps.support.MessageServiceTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocationController.class)
@Import(GlobalExceptionHandler.class)
class LocationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	@MockitoBean
	private LocationProducer producer;

	@MockitoBean
	private LocationService locationService;

	@MockitoBean
	private MessageService messages;

	@BeforeEach
	void setUp() {
		MessageServiceTestSupport.stubEnglish(messages);
	}

	@Test
	void shouldAcceptLocationSend() throws Exception {
		LocationDto dto = new LocationDto("car-1", 52.1, 21.0, Instant.parse("2026-01-01T12:00:00Z"));

		mockMvc.perform(post("/location/send")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(dto)))
				.andExpect(status().isAccepted());

		verify(producer).send(dto);
	}

	@Test
	void shouldReturnLatest() throws Exception {
		when(locationService.getLatest("car-1"))
				.thenReturn(new LocationDto("car-1", 52.1, 21.0, Instant.parse("2026-01-01T12:00:00Z")));

		mockMvc.perform(get("/location/{externalId}/latest", "car-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.latitude").value(52.1))
				.andExpect(jsonPath("$.longitude").value(21.0));
	}

	@Test
	void shouldReturnHistory() throws Exception {
		when(locationService.getHistory("car-1", null, null)).thenReturn(List.of(
				new LocationDto("car-1", 1, 1, Instant.parse("2026-01-01T10:00:00Z")),
				new LocationDto("car-1", 2, 2, Instant.parse("2026-01-01T11:00:00Z"))
		));

		mockMvc.perform(get("/location/{externalId}/history", "car-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void shouldReturnTrack() throws Exception {
		TrackSummaryDto track = new TrackSummaryDto(
				"car-1",
				2,
				120.5,
				Instant.parse("2026-01-01T10:00:00Z"),
				Instant.parse("2026-01-01T11:00:00Z"),
				List.of()
		);
		when(locationService.getTrack("car-1", null, null)).thenReturn(track);

		mockMvc.perform(get("/location/{externalId}/track", "car-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.pointCount").value(2))
				.andExpect(jsonPath("$.totalDistanceMeters").value(120.5));
	}

	@Test
	void shouldReturnLatestForAll() throws Exception {
		when(locationService.getLatestForAllDevices()).thenReturn(List.of(
				new LocationDto("a", 1, 1, Instant.now()),
				new LocationDto("b", 2, 2, Instant.now())
		));

		mockMvc.perform(get("/location/latest/all"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void shouldReturn404_whenLatestMissing() throws Exception {
		when(locationService.getLatest("missing"))
				.thenThrow(new NotFoundException("No location found for device: missing"));

		mockMvc.perform(get("/location/{externalId}/latest", "missing"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void shouldReturn400_whenInvalidRange() throws Exception {
		when(locationService.getHistory(
				org.mockito.ArgumentMatchers.eq("car-1"),
				org.mockito.ArgumentMatchers.any(),
				org.mockito.ArgumentMatchers.any()
		)).thenThrow(new IllegalArgumentException("'from' must be before or equal to 'to'"));

		mockMvc.perform(get("/location/{externalId}/history", "car-1")
						.param("from", "2026-01-02T00:00:00Z")
						.param("to", "2026-01-01T00:00:00Z"))
				.andExpect(status().isBadRequest());
	}
}
