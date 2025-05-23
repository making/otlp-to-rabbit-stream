package am.ik.otlp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.proto.logs.v1.LogsData;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
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

	private final OtlpSinkProps props;

	private final JsonFormat.Printer printer = JsonFormat.printer().omittingInsignificantWhitespace();

	public LogsV1Controller(RabbitStreamTemplate rabbitStreamTemplate, ObjectMapper objectMapper, OtlpSinkProps props) {
		this.rabbitStreamTemplate = rabbitStreamTemplate;
		this.objectMapper = objectMapper;
		this.props = props;
	}

	@PostMapping(path = "/v1/logs",
			consumes = { MediaType.APPLICATION_PROTOBUF_VALUE, MediaType.APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.ACCEPTED)
	public CompletableFuture<Void> logs(@RequestBody LogsData logs) {
		List<CompletableFuture<?>> sent = new ArrayList<>();
		try {
			switch (props.getFormat()) {
				case FLATTEN -> {
					for (Log log : Log.flatten(logs)) {
						sent.add(this.rabbitStreamTemplate.convertAndSend(this.objectMapper.writeValueAsBytes(log)));
					}
				}
				case OTLP -> sent.add(this.rabbitStreamTemplate.convertAndSend(logs.toByteArray()));
				case OTLP_JSON -> sent.add(
						this.rabbitStreamTemplate.convertAndSend(printer.print(logs).getBytes(StandardCharsets.UTF_8)));
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return CompletableFuture.allOf(sent.toArray(new CompletableFuture[0]));
	}

}
