package am.ik.logs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OtlpLogToRabbitStreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(OtlpLogToRabbitStreamApplication.class, args);
	}

}
