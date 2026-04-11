package gps.service.consumer;

import gps.config.RabbitConfig;
import gps.dto.DeviceDto;
import gps.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceConsumer {

	private final DeviceService deviceService;

	@RabbitListener(queues = RabbitConfig.DEVICE_QUEUE)
	public void consume(final DeviceDto dto) {
		deviceService.handleIncomingDevice(dto);
	}
}
