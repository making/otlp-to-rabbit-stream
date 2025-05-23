package am.ik.otlp;

import java.util.concurrent.atomic.AtomicReference;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

public class TestReceiver {

	private static final AtomicReference<byte[]> received = new AtomicReference<>(null);

	@RabbitListener(queues = "${spring.rabbitmq.stream.name}")
	void listen(byte[] data) {
		received.set(data);
	}

	public static byte[] getReceived() {
		return received.get();
	}

	public static void clearReceived() {
		received.set(null);
	}

}
