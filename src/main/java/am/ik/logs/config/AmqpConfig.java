package am.ik.logs.config;

import com.rabbitmq.stream.ByteCapacity;
import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.compression.Compression;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.rabbit.stream.listener.ConsumerCustomizer;
import org.springframework.rabbit.stream.producer.ProducerCustomizer;
import org.springframework.rabbit.stream.support.StreamAdmin;

@Configuration(proxyBeanMethods = false)
public class AmqpConfig {

	@Bean
	StreamAdmin streamAdmin(Environment streamEnvironment, @Value("${spring.rabbitmq.stream.name}") String streamName) {
		return new StreamAdmin(streamEnvironment,
				sc -> sc.stream(streamName)
					.maxSegmentSizeBytes(ByteCapacity.MB(100))
					.maxLengthBytes(ByteCapacity.GB(1))
					.create());
	}

	@Bean
	ProducerCustomizer producerCustomizer() {
		return (beanName,
				producerBuilder) -> producerBuilder.compression(Compression.ZSTD).batchSize(100).subEntrySize(10);
	}

	@Bean
	ConsumerCustomizer consumerCustomizer() {
		return (beanName, consumerBuilder) -> consumerBuilder.name("demo").autoTrackingStrategy();
	}

}
