package am.ik.logs.config;

import am.ik.logs.LogSinkProps;
import com.rabbitmq.stream.ByteCapacity;
import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.StreamCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.rabbit.stream.producer.ProducerCustomizer;
import org.springframework.rabbit.stream.support.StreamAdmin;
import org.springframework.util.unit.DataSize;

@Configuration(proxyBeanMethods = false)
public class AmqpConfig {

	private final LogSinkProps props;

	public AmqpConfig(LogSinkProps props) {
		this.props = props;
	}

	@Bean
	StreamAdmin streamAdmin(Environment streamEnvironment, @Value("${spring.rabbitmq.stream.name}") String streamName) {
		return new StreamAdmin(streamEnvironment, sc -> {
			PropertyMapper mapper = PropertyMapper.get();
			StreamCreator streamCreator = sc.stream(streamName);
			LogSinkProps.Stream streamProps = props.getStream();
			mapper.from(streamProps.getMaxSegmentSize())
				.whenNonNull()
				.as(DataSize::toBytes)
				.as(ByteCapacity::B)
				.to(streamCreator::maxSegmentSizeBytes);
			mapper.from(streamProps.getMaxLength())
				.whenNonNull()
				.as(DataSize::toBytes)
				.as(ByteCapacity::B)
				.to(streamCreator::maxLengthBytes);
			mapper.from(streamProps.getMaxAge()).whenNonNull().to(streamCreator::maxAge);
			streamCreator.create();
		});
	}

	@Bean
	ProducerCustomizer producerCustomizer() {
		return (beanName, builder) -> {
			PropertyMapper mapper = PropertyMapper.get();
			LogSinkProps.Producer producerProps = props.getProducer();
			mapper.from(producerProps.getCompression()).whenNonNull().to(builder::compression);
			mapper.from(producerProps.getBatchSize()).when(x -> x > 0).to(builder::batchSize);
			mapper.from(producerProps.getSubEntrySize()).when(x -> x > 0).to(builder::subEntrySize);
		};
	}

}
