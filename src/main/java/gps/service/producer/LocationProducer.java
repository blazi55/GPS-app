package gps.service.producer;

import gps.config.RabbitConfig;
import gps.dto.LocationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationProducer {

	private final RabbitTemplate rabbitTemplate;

	public void send(final LocationDto locationDto) {
		rabbitTemplate.convertAndSend(
				RabbitConfig.EXCHANGE,
				RabbitConfig.LOCATION_ROUTING_KEY,
				locationDto
		);
	}
}
