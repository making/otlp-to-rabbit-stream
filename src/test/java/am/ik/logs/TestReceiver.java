package am.ik.logs;

import java.util.concurrent.atomic.AtomicReference;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

public class TestReceiver {

	static AtomicReference<String> received = new AtomicReference<>(null);

	@RabbitListener(queues = "${spring.rabbitmq.stream.name}")
	void listen(String data) {
		received.set(data);
	}

}
