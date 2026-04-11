package gps.service.producer;

import gps.config.RabbitConfig;
import gps.dto.DeviceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceProducer {

	private final RabbitTemplate rabbitTemplate;

	public void send(final DeviceDto deviceDto) {
		rabbitTemplate.convertAndSend(
				RabbitConfig.EXCHANGE,
				RabbitConfig.DEVICE_ROUTING_KEY,
				deviceDto
		);
	}
}
