package gps.service.consumer;

import gps.config.RabbitConfig;
import gps.dto.DeviceDto;
import gps.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceConsumer {

	private final DeviceService deviceService;

	@RabbitListener(queues = RabbitConfig.DEVICE_QUEUE)
	public void consume(final DeviceDto dto) {
		log.info("Device Location: {}", dto);
		deviceService.handleIncomingDevice(dto);
	}
}
