package am.ik.logs;

import com.rabbitmq.stream.compression.Compression;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.lang.Nullable;
import org.springframework.util.unit.DataSize;

@ConfigurationProperties(prefix = "log-sink")
public final class LogSinkProps {

	private LogSinkFormat format = LogSinkFormat.FLATTEN;

	@NestedConfigurationProperty
	private final Stream stream = new Stream();

	@NestedConfigurationProperty
	private final Producer producer = new Producer();

	public Stream getStream() {
		return stream;
	}

	public Producer getProducer() {
		return producer;
	}

	public LogSinkFormat getFormat() {
		return format;
	}

	public void setFormat(LogSinkFormat format) {
		this.format = format;
	}

	public static final class Stream {

		@Nullable
		private DataSize maxSegmentSize = DataSize.ofMegabytes(100);

		@Nullable
		private DataSize maxLength = DataSize.ofGigabytes(1);

		@Nullable
		private Duration maxAge = null;

		@Nullable
		public DataSize getMaxSegmentSize() {
			return maxSegmentSize;
		}

		public void setMaxSegmentSize(DataSize maxSegmentSize) {
			this.maxSegmentSize = maxSegmentSize;
		}

		@Nullable
		public DataSize getMaxLength() {
			return maxLength;
		}

		public void setMaxLength(DataSize maxLength) {
			this.maxLength = maxLength;
		}

		@Nullable
		public Duration getMaxAge() {
			return maxAge;
		}

		public void setMaxAge(Duration maxAge) {
			this.maxAge = maxAge;
		}

	}

	public static final class Producer {

		@Nullable
		private Compression compression = Compression.GZIP;

		private int batchSize = 100;

		private int subEntrySize = 10;

		@Nullable
		public Compression getCompression() {
			return compression;
		}

		public void setCompression(Compression compression) {
			this.compression = compression;
		}

		public int getBatchSize() {
			return batchSize;
		}

		public void setBatchSize(int batchSize) {
			this.batchSize = batchSize;
		}

		public int getSubEntrySize() {
			return subEntrySize;
		}

		public void setSubEntrySize(int subEntrySize) {
			this.subEntrySize = subEntrySize;
		}

	}

}
