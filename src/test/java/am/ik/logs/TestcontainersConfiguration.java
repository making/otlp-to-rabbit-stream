package am.ik.logs;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	RabbitMQContainer rabbitContainer(@Value("${rabbitmq.management.exposed-port:0}") int managementExposedPort) {
		RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(DockerImageName.parse("rabbitmq:management-alpine"))
			.withEnv("RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS", "-rabbitmq_stream advertised_host localhost")
			.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("rabbitmq")))
			.withExposedPorts(5672, 5671, 15671, 15672, 5552);
		if (managementExposedPort > 0) {
			ReflectionTestUtils.invokeMethod(rabbitMQContainer, "addFixedExposedPort", managementExposedPort, 15672);
		}
		return rabbitMQContainer;
	}

	@Bean
	InitializingBean rabbitContainerInitializingBean(RabbitMQContainer rabbitContainer) {
		return () -> rabbitContainer.execInContainer("rabbitmq-plugins", "enable", "rabbitmq_stream",
				"rabbitmq_stream_management");
	}

	@Bean
	DynamicPropertyRegistrar dynamicPropertyRegistrar(RabbitMQContainer rabbitContainer) {
		return registry -> {
			registry.add("spring.rabbitmq.stream.host", () -> "localhost");
			registry.add("spring.rabbitmq.stream.port", () -> rabbitContainer.getMappedPort(5552));
		};
	}

}