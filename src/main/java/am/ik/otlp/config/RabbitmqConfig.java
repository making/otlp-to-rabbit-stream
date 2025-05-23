package am.ik.otlp.config;

import am.ik.otlp.OtlpSinkProps;
import com.rabbitmq.stream.ByteCapacity;
import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.StreamCreator;
import org.springframework.boot.autoconfigure.amqp.RabbitStreamTemplateConfigurer;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.rabbit.stream.producer.ProducerCustomizer;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;
import org.springframework.rabbit.stream.support.StreamAdmin;
import org.springframework.util.unit.DataSize;

@Configuration(proxyBeanMethods = false)
public class RabbitmqConfig {

	private final OtlpSinkProps props;

	public RabbitmqConfig(OtlpSinkProps props) {
		this.props = props;
	}

	@Bean
	StreamAdmin streamAdmin(Environment streamEnvironment) {
		return new StreamAdmin(streamEnvironment, sc -> {
			PropertyMapper mapper = PropertyMapper.get();
			createStream(sc, mapper, this.props.getLogs().getStreamName());
		});
	}

	void createStream(StreamCreator sc, PropertyMapper mapper, String streamName) {
		StreamCreator streamCreator = sc.stream(streamName);
		OtlpSinkProps.Stream streamProps = props.getStream();
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
	}

	@Bean
	ProducerCustomizer producerCustomizer() {
		return (beanName, builder) -> {
			PropertyMapper mapper = PropertyMapper.get();
			OtlpSinkProps.Producer producerProps = props.getProducer();
			mapper.from(producerProps.getCompression()).whenNonNull().to(builder::compression);
			mapper.from(producerProps.getBatchSize()).when(x -> x > 0).to(builder::batchSize);
			mapper.from(producerProps.getSubEntrySize()).when(x -> x > 0).to(builder::subEntrySize);
		};
	}

	@Bean
	RabbitStreamTemplate rabbitStreamTemplateForLogs(Environment rabbitStreamEnvironment,
			RabbitStreamTemplateConfigurer configurer) {
		RabbitStreamTemplate template = new RabbitStreamTemplate(rabbitStreamEnvironment,
				this.props.getLogs().getStreamName());
		configurer.configure(template);
		return template;
	}

}
