package am.ik.logs;

import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.proto.logs.v1.LogsData;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

public class Fixtures {

	static String json() {
		try (InputStream stream = new ClassPathResource("logs.json").getInputStream()) {
			return StreamUtils.copyToString(stream, StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	static LogsData logsData() {
		return fromJson(json());
	}

	static LogsData fromJson(String json) {
		LogsData.Builder builder = LogsData.newBuilder();
		try {
			JsonFormat.parser().merge(json, builder);
		}
		catch (com.google.protobuf.InvalidProtocolBufferException e) {
			throw new UncheckedIOException(e);
		}
		return builder.build();
	}

}
