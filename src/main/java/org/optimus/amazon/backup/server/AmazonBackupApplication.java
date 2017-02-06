package org.optimus.amazon.backup.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@SpringBootApplication
@Configuration
@EnableAsync
@EnableWebSecurity
@ComponentScan(basePackages = { "org.optimus.amazon.backup.server.resources",
		"org.optimus.amazon.backup.server.services" })
public class AmazonBackupApplication extends WebSecurityConfigurerAdapter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AmazonBackupApplication.class);

	public static ConfigurableApplicationContext context;

	@Bean
	public BasicAuthenticationProvider getBasicAuthenticationProvider() {
		return new BasicAuthenticationProvider();
	}

	public static void main(String[] args) {
		LOGGER.info("Starting application ...");
		context = SpringApplication.run(AmazonBackupApplication.class, args);
		LOGGER.info("Application started");
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http //
				.authorizeRequests().anyRequest().authenticated().and() //
				.httpBasic().and() //
				.headers().frameOptions().sameOrigin().httpStrictTransportSecurity().disable().and() //
				.csrf().disable();
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(getBasicAuthenticationProvider());
	}
}
