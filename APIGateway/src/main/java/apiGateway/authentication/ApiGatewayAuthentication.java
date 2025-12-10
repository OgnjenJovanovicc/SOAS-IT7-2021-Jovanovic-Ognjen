package apiGateway.authentication;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

import api.dtos.UserDto;

@Configuration
@EnableWebFluxSecurity
public class ApiGatewayAuthentication {
	
	@Bean
	SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
		http
		.csrf(csrf -> csrf.disable())
		.authorizeExchange(exchange -> exchange
				.pathMatchers("/currency-exchange").permitAll()
				.pathMatchers("/crypto-exchange").permitAll()
				
				.pathMatchers("/currency-conversion").hasRole("USER")
				.pathMatchers("/crypto-conversion").hasRole("USER")
				
				.pathMatchers(HttpMethod.POST,"/bank-account").hasAnyRole("ADMIN")
				.pathMatchers("/bank-account").hasAnyRole("ADMIN","USER")
				
				.pathMatchers(HttpMethod.PUT,"/bank-account/**").hasAnyRole("ADMIN")
				.pathMatchers(HttpMethod.DELETE,"/bank-account/**").hasAnyRole("ADMIN")
				
				.pathMatchers(HttpMethod.POST,"/crypto-wallet").hasAnyRole("ADMIN")
				.pathMatchers("/crypto-wallet").hasAnyRole("ADMIN","USER")
				.pathMatchers(HttpMethod.POST,"/crypto-wallet").hasAnyRole("ADMIN")
				.pathMatchers(HttpMethod.PUT,"/crypto-wallet/**").hasAnyRole("ADMIN")
				.pathMatchers(HttpMethod.DELETE,"/crypto-wallet/**").hasAnyRole("ADMIN")
				
				.pathMatchers(HttpMethod.POST,"/users/newAdmin").hasRole("OWNER")
				.pathMatchers("/users").hasAnyRole("ADMIN","OWNER")
				.pathMatchers(HttpMethod.POST,"/users/newUser").hasAnyRole("OWNER","ADMIN")
				.pathMatchers(HttpMethod.PUT,"/users/**").hasAnyRole("OWNER","ADMIN")
				.pathMatchers(HttpMethod.DELETE,"/users/**").hasAnyRole("OWNER")
				
				.pathMatchers("/trade-exchange").hasAnyRole("USER")
				.pathMatchers("/trade-conversion").hasAnyRole("USER")
				
				).httpBasic(Customizer.withDefaults());
		
		return http.build();
	}
	
	@Bean
	ReactiveUserDetailsService reactiveUserDetailsService(WebClient.Builder webClientBuilder, BCryptPasswordEncoder encoder) {
		WebClient client = webClientBuilder.baseUrl("http://localhost:8770").build();
			
		return user -> client.get()
				.uri(uriBuilder -> uriBuilder
						.path("/users/email")
						.queryParam("email", user)
						.build()
				)
				.retrieve()
				.bodyToMono(UserDto.class)
				.map(dto -> User.withUsername(dto.getEmail())
						.password(encoder.encode(dto.getPassword()))
						.roles(dto.getRole())
						.build()
				);
			
		}
		

	@Bean
	BCryptPasswordEncoder getEncoder() {
		return new BCryptPasswordEncoder();
	}

}
