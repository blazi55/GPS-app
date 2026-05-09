package gps.service.consumer;

import gps.config.RabbitConfig;
import gps.dto.LocationDto;
import gps.service.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationConsumer {

	private final LocationService locationService;

	@RabbitListener(queues = RabbitConfig.LOCATION_QUEUE)
	public void consume(final LocationDto dto) {
		log.info("Consume Location: {}", dto);
		locationService.handleIncomingLocation(dto);
	}
}
