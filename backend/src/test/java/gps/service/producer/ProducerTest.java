package gps.service.producer;

import gps.config.RabbitConfig;
import gps.dto.DeviceDto;
import gps.dto.LocationDto;
import gps.enums.DeviceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProducerTest {

	@Mock
	private RabbitTemplate rabbitTemplate;

	@Test
	void shouldPublishDeviceMessage() {
		DeviceProducer producer = new DeviceProducer(rabbitTemplate);
		DeviceDto dto = new DeviceDto(null, "Car", DeviceType.CAR, "car-1");

		producer.send(dto);

		verify(rabbitTemplate).convertAndSend(
				RabbitConfig.EXCHANGE,
				RabbitConfig.DEVICE_ROUTING_KEY,
				dto
		);
	}

	@Test
	void shouldPublishLocationMessage() {
		LocationProducer producer = new LocationProducer(rabbitTemplate);
		LocationDto dto = new LocationDto("car-1", 52.0, 21.0, Instant.now());

		producer.send(dto);

		verify(rabbitTemplate).convertAndSend(
				RabbitConfig.EXCHANGE,
				RabbitConfig.LOCATION_ROUTING_KEY,
				dto
		);
	}
}
