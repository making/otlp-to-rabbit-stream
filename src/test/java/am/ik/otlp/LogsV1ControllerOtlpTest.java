package am.ik.otlp;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.proto.logs.v1.LogsData;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = { "logging.structured.format.console=", "otlp-sink.format=otlp",
				"otlp-sink.producer.compression=zstd" })
@Import({ TestcontainersConfiguration.class })
class LogsV1ControllerOtlpTest {

	RestClient restClient;

	String json = Fixtures.json();

	LogsData logsData = Fixtures.logsData();

	@Autowired
	ObjectMapper objectMapper;

	@BeforeEach
	public void init(@Autowired RestClient.Builder restClientBuilder,
			@Autowired LogbookClientHttpRequestInterceptor logbookClientHttpRequestInterceptor,
			@LocalServerPort int port) {
		if (this.restClient == null) {
			this.restClient = restClientBuilder.baseUrl("http://localhost:" + port)
				.defaultStatusHandler(__ -> true, (req, res) -> {
				})
				.requestInterceptor(logbookClientHttpRequestInterceptor)
				.build();
		}
		TestReceiver.clearReceived();
	}

	@Test
	void ingestProtobuf() throws Exception {
		ResponseEntity<Void> response = this.restClient.post()
			.uri("/v1/logs")
			.contentType(MediaType.APPLICATION_PROTOBUF)
			.body(logsData)
			.retrieve()
			.toBodilessEntity();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
		Awaitility.await().untilAsserted(() -> assertThat(TestReceiver.getReceived()).isNotNull());
		assertThat(LogsData.parseFrom(TestReceiver.getReceived())).isEqualTo(logsData);
	}

	@Test
	void ingestProtobufGzip() throws Exception {
		ResponseEntity<Void> response = this.restClient.post()
			.uri("/v1/logs")
			.contentType(MediaType.APPLICATION_PROTOBUF)
			.header(HttpHeaders.CONTENT_ENCODING, "gzip")
			.body(compress(logsData.toByteArray()))
			.retrieve()
			.toBodilessEntity();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
		Awaitility.await().untilAsserted(() -> assertThat(TestReceiver.getReceived()).isNotNull());
		assertThat(LogsData.parseFrom(TestReceiver.getReceived())).isEqualTo(logsData);
	}

	@Test
	void ingestJson() throws Exception {
		ResponseEntity<Void> response = this.restClient.post()
			.uri("/v1/logs")
			.contentType(MediaType.APPLICATION_JSON)
			.body(json)
			.retrieve()
			.toBodilessEntity();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
		Awaitility.await().untilAsserted(() -> assertThat(TestReceiver.getReceived()).isNotNull());
		assertThat(LogsData.parseFrom(TestReceiver.getReceived())).isEqualTo(logsData);
	}

	static byte[] compress(byte[] body) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos)) {
			gzipOutputStream.write(body);
		}
		return baos.toByteArray();
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class TestConfig {

		@Bean
		TestReceiver testReceiver() {
			return new TestReceiver();
		}

	}

}