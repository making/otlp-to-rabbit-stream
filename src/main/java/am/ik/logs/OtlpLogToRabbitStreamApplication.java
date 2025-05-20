package am.ik.logs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OtlpLogToRabbitStreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(OtlpLogToRabbitStreamApplication.class, args);
	}

}
