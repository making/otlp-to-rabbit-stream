package am.ik.otlp.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration(proxyBeanMethods = false)
public class SecurityConfig {

	@Bean
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
		return http
			.authorizeHttpRequests((authorize) -> authorize.requestMatchers(EndpointRequest.toAnyEndpoint())
				.permitAll()
				.anyRequest()
				.authenticated())
			.httpBasic(Customizer.withDefaults())
			.csrf(AbstractHttpConfigurer::disable)
			.build();
	}

}
