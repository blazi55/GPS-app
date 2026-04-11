package gps.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;

@Configuration
public class RabbitConfig {

	public static final String EXCHANGE = "gps.exchange";
	public static final String LOCATION_ROUTING_KEY = "gps.location.routing";
	public static final String DEVICE_ROUTING_KEY = "gps.device.routing";
	public static final String LOCATION_QUEUE = "gps.location.queue";
	public static final String DEVICE_QUEUE = "gps.device.queue";

	@Bean
	public Queue locationQueue() {
		return new Queue(LOCATION_QUEUE, true);
	}

	@Bean
	public Queue deviceQueue() {
		return new Queue(DEVICE_QUEUE, true);
	}

	@Bean
	public DirectExchange exchange() {
		return new DirectExchange(EXCHANGE);
	}

	@Bean
	public Binding locationBinding(Queue locationQueue, DirectExchange exchange) {
		return BindingBuilder
				.bind(locationQueue)
				.to(exchange)
				.with(LOCATION_ROUTING_KEY);
	}

	@Bean
	public Binding deviceBinding(Queue deviceQueue, DirectExchange exchange) {
		return BindingBuilder
				.bind(deviceQueue)
				.to(exchange)
				.with(DEVICE_ROUTING_KEY);
	}

	@Bean
	public Jackson2JsonMessageConverter messageConverter() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule()); // 🔥 TO!

		return new Jackson2JsonMessageConverter(mapper);
	}
}
