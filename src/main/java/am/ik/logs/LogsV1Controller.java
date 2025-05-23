package am.ik.logs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.proto.logs.v1.LogsData;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogsV1Controller {

	private final RabbitStreamTemplate rabbitStreamTemplate;

	private final ObjectMapper objectMapper;

	private final LogSinkProps props;

	public LogsV1Controller(RabbitStreamTemplate rabbitStreamTemplate, ObjectMapper objectMapper, LogSinkProps props) {
		this.rabbitStreamTemplate = rabbitStreamTemplate;
		this.objectMapper = objectMapper;
		this.props = props;
	}

	@PostMapping(path = "/v1/logs",
			consumes = { MediaType.APPLICATION_PROTOBUF_VALUE, MediaType.APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.ACCEPTED)
	public CompletableFuture<Void> logs(@RequestBody LogsData logs) {
		List<CompletableFuture<?>> sent = new ArrayList<>();
		switch (props.getMode()) {
			case FLATTEN -> {
				for (Log log : Log.flatten(logs)) {
					try {
						sent.add(this.rabbitStreamTemplate.convertAndSend(this.objectMapper.writeValueAsBytes(log)));
					}
					catch (JsonProcessingException e) {
						throw new UncheckedIOException(e);
					}
				}
			}
			case PROXY -> sent.add(this.rabbitStreamTemplate.convertAndSend(logs.toByteArray()));
		}
		return CompletableFuture.allOf(sent.toArray(new CompletableFuture[0]));
	}

}
