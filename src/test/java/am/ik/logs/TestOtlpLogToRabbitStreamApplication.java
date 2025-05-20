package am.ik.logs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestOtlpLogToRabbitStreamApplication {

	public static void main(String[] args) {
		SpringApplication.from(OtlpLogToRabbitStreamApplication::main).with(TestOtlpLogToRabbitStreamApplication.class).run(args);
	}

}
