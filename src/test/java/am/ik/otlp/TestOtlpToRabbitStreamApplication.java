package am.ik.otlp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestOtlpToRabbitStreamApplication {

	public static void main(String[] args) {
		String[] newArgs = new String[args.length + 1];
		System.arraycopy(args, 0, newArgs, 0, args.length);
		newArgs[args.length] = "---rabbitmq.management.exposed-port=35672";
		SpringApplication.from(OtlpToRabbitStreamApplication::main)
			.with(TestcontainersConfiguration.class)
			.run(newArgs);
	}

}
