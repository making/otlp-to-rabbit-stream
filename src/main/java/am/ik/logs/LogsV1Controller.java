package am.ik.logs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.logs.v1.LogRecord;
import io.opentelemetry.proto.logs.v1.LogsData;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.proto.logs.v1.ScopeLogs;
import io.opentelemetry.proto.resource.v1.Resource;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
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

	private static final String SERVICE_NAME_ATTR = "service.name";

	public LogsV1Controller(RabbitStreamTemplate rabbitStreamTemplate, ObjectMapper objectMapper) {
		this.rabbitStreamTemplate = rabbitStreamTemplate;
		this.objectMapper = objectMapper;
	}

	@PostMapping(path = "/v1/logs",
			consumes = { MediaType.APPLICATION_PROTOBUF_VALUE, MediaType.APPLICATION_JSON_VALUE })
	@ResponseStatus(HttpStatus.ACCEPTED)
	public CompletableFuture<Void> logs(@RequestBody LogsData logs) {
		List<CompletableFuture<?>> sent = new ArrayList<>();
		for (int i = 0; i < logs.getResourceLogsCount(); i++) {
			ResourceLogs resourceLogs = logs.getResourceLogs(i);
			Map<String, Object> resourceAttributes = new HashMap<>();
			String serviceName = "";
			Resource resource = resourceLogs.getResource();
			if (resource.getAttributesCount() > 0) {
				for (KeyValue attribute : resource.getAttributesList()) {
					if (SERVICE_NAME_ATTR.equals(attribute.getKey())) {
						serviceName = anyToObject(attribute.getValue()).toString();
					}
					else {
						resourceAttributes.put(attribute.getKey(), anyToObject(attribute.getValue()));
					}
				}
			}
			for (int j = 0; j < resourceLogs.getScopeLogsCount(); j++) {
				ScopeLogs scopeLogs = resourceLogs.getScopeLogs(j);
				Map<String, Object> scopeAttributes = new HashMap<>();
				InstrumentationScope scope = scopeLogs.getScope();
				if (scope.getAttributesCount() > 0) {
					scope.getAttributesList()
						.forEach(attribute -> scopeAttributes.put(attribute.getKey(),
								anyToObject(attribute.getValue())));
				}
				for (int k = 0; k < scopeLogs.getLogRecordsCount(); k++) {
					LogBuilder logBuilder = LogBuilder.log()
						.scope(scope.getName())
						.serviceName(serviceName)
						.resourceAttributes(resourceAttributes);
					Map<String, Object> attributes = new HashMap<>(scopeAttributes);
					LogRecord logRecord = scopeLogs.getLogRecords(k);
					logBuilder.timestamp(Instant.EPOCH.plusNanos(logRecord.getTimeUnixNano()));
					logBuilder.severity(logRecord.getSeverityText());
					logBuilder.body(anyToObject(logRecord.getBody()).toString() /* TODO */);
					logBuilder.traceId(HexFormat.of().formatHex(logRecord.getTraceId().toByteArray()));
					logBuilder.spanId(HexFormat.of().formatHex(logRecord.getSpanId().toByteArray()));
					if (logRecord.getAttributesCount() > 0) {
						logRecord.getAttributesList()
							.forEach(
									attribute -> attributes.put(attribute.getKey(), anyToObject(attribute.getValue())));
						logBuilder.attributes(attributes);
					}
					else {
						logBuilder.attributes(Map.of());
					}
					try {
						sent.add(this.rabbitStreamTemplate
							.convertAndSend(this.objectMapper.writeValueAsBytes(logBuilder.build())));
					}
					catch (JsonProcessingException e) {
						throw new UncheckedIOException(e);
					}
				}
			}
		}
		return CompletableFuture.allOf(sent.toArray(new CompletableFuture[0]));
	}

	static Object anyToObject(AnyValue value) {
		AnyValue.ValueCase valueCase = value.getValueCase();
		return switch (valueCase) {
			case STRING_VALUE -> value.getStringValue();
			case BOOL_VALUE -> value.getBoolValue();
			case BYTES_VALUE -> value.getBytesValue().toByteArray();
			case INT_VALUE -> value.getIntValue();
			case DOUBLE_VALUE -> value.getDoubleValue();
			case ARRAY_VALUE ->
				value.getArrayValue().getValuesList().stream().map(LogsV1Controller::anyToObject).toList();
			case KVLIST_VALUE -> Map.ofEntries(value.getKvlistValue()
				.getValuesList()
				.stream()
				.map(kv -> Map.entry(kv.getKey(), anyToObject(kv.getValue())))
				.toArray(Map.Entry[]::new));
			case VALUE_NOT_SET -> "";
		};
	}

}
