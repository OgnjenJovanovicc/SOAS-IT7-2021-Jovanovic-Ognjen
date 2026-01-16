package apiGateway.authentication;


import java.util.Base64;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

import api.dtos.UserDto;
import reactor.core.publisher.Mono;

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
				.pathMatchers(HttpMethod.GET,"/bank-account").hasAnyRole("ADMIN")
				.pathMatchers(HttpMethod.POST,"/bank-account").hasAnyRole("ADMIN")
				.pathMatchers(HttpMethod.GET,"/bank-account/**").hasAnyRole("USER")
				.pathMatchers("/bank-account/**").hasAnyRole("ADMIN","USER")
				.pathMatchers(HttpMethod.PUT,"/bank-account/**").hasAnyRole("ADMIN")
				.pathMatchers(HttpMethod.DELETE,"/bank-account/**").hasAnyRole("ADMIN")
				.pathMatchers(HttpMethod.GET,"/bank-account").hasAnyRole("ADMIN")
				.pathMatchers(HttpMethod.POST,"/crypto-wallet").hasAnyRole("ADMIN")
				.pathMatchers("/crypto-wallet").hasAnyRole("ADMIN","USER")
				.pathMatchers(HttpMethod.POST,"/crypto-wallet").hasAnyRole("ADMIN")
				.pathMatchers(HttpMethod.PUT,"/crypto-wallet/**").hasAnyRole("ADMIN")
				.pathMatchers(HttpMethod.DELETE,"/crypto-wallet/**").hasAnyRole("ADMIN")
				.pathMatchers(HttpMethod.GET,"/crypto-wallet/**").hasAnyRole("USER")
				
				.pathMatchers(HttpMethod.POST,"/users/newAdmin").hasRole("OWNER")
				.pathMatchers("/users").hasAnyRole("ADMIN","OWNER")
				.pathMatchers(HttpMethod.POST,"/users/newUser").hasAnyRole("OWNER","ADMIN")
				.pathMatchers(HttpMethod.PUT,"/users/**").hasAnyRole("OWNER","ADMIN")
				.pathMatchers(HttpMethod.DELETE,"/users/**").hasAnyRole("OWNER")
				.pathMatchers(HttpMethod.GET, "/users/**").hasAnyRole("USER")
				.pathMatchers(HttpMethod.GET, "/users").hasAnyRole("ADMIN")
				.pathMatchers("/trade/**").hasAnyRole("USER")
				.pathMatchers("/trade/**").hasAnyRole("USER")
				
				).httpBasic(Customizer.withDefaults());
		
		return http.build();
	}

	@Bean
    ReactiveUserDetailsService reactiveUserDetailsService(WebClient.Builder webClientBuilder) {
        WebClient client = webClientBuilder
                //.baseUrl("http://localhost:8770")
                .baseUrl("http://users-service:8770") // za Docker
                .build();

        return username -> {
            return client.get()
                   // .uri("/users/auth/{email}", username)
            		.uri(uriBuilder -> uriBuilder
                            .path("/users/auth")
                            .queryParam("email", username)
                            .build())
                    .retrieve()
                    .bodyToMono(UserDto.class)
                    .map(dto -> {
                        String password = dto.getPassword();
                        if (!password.startsWith("{") && !password.contains("}")) {
                            password = "{noop}" + password;
                        }
                        
                        return User.withUsername(dto.getEmail())
                                .password(password)
                                .roles(dto.getRole())
                                .build();
                    })
                    .onErrorResume(e -> {
                        System.err.println("Error fetching user for authentication: " + e.getMessage());
                        return Mono.empty(); 
                    });
        };
    }
    
    @Bean
    ReactiveAuthenticationManager authenticationManager(ReactiveUserDetailsService userDetailsService) {
        return new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
    }
	@Bean
	BCryptPasswordEncoder getEncoder() {
		return new BCryptPasswordEncoder();
	}

}
