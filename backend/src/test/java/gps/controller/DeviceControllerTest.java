package gps.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import gps.dto.DeviceDto;
import gps.dto.SendDeviceDto;
import gps.enums.DeviceType;
import gps.exception.GlobalExceptionHandler;
import gps.exception.NotFoundException;
import gps.i18n.MessageService;
import gps.service.DeviceService;
import gps.service.producer.DeviceProducer;
import gps.support.MessageServiceTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceController.class)
@Import(GlobalExceptionHandler.class)
class DeviceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	@MockitoBean
	private DeviceProducer producer;

	@MockitoBean
	private DeviceService deviceService;

	@MockitoBean
	private MessageService messages;

	@BeforeEach
	void setUp() {
		MessageServiceTestSupport.stubEnglish(messages);
	}

	@Test
	void shouldAcceptDeviceSend() throws Exception {
		SendDeviceDto send = new SendDeviceDto("Car", "car-1", DeviceType.CAR);
		DeviceDto mapped = new DeviceDto(null, "Car", DeviceType.CAR, "car-1");
		when(deviceService.mapSendToDto(any(SendDeviceDto.class))).thenReturn(mapped);

		mockMvc.perform(post("/device/send")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(send)))
				.andExpect(status().isAccepted());

		verify(producer).send(mapped);
	}

	@Test
	void shouldReturnDeviceById() throws Exception {
		when(deviceService.getDevice(1L))
				.thenReturn(new DeviceDto(1L, "Phone", DeviceType.PHONE, "p-1"));

		mockMvc.perform(get("/device/get/{id}", 1L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Phone"))
				.andExpect(jsonPath("$.externalId").value("p-1"));
	}

	@Test
	void shouldReturnDeviceByExternalId() throws Exception {
		when(deviceService.getDeviceByExternalId("p-1"))
				.thenReturn(new DeviceDto(1L, "Phone", DeviceType.PHONE, "p-1"));

		mockMvc.perform(get("/device/get/external/{externalId}", "p-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1));
	}

	@Test
	void shouldReturn404_whenDeviceMissing() throws Exception {
		when(deviceService.getDevice(99L))
				.thenThrow(new NotFoundException("Device not found for id: 99"));

		mockMvc.perform(get("/device/get/{id}", 99L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("Device not found for id: 99"));
	}

	@Test
	void shouldReturnAllDevices() throws Exception {
		when(deviceService.getAllDevices()).thenReturn(List.of(
				new DeviceDto(1L, "A", DeviceType.PHONE, "a"),
				new DeviceDto(2L, "B", DeviceType.CAR, "b")
		));

		mockMvc.perform(get("/device/get/all"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}
}
