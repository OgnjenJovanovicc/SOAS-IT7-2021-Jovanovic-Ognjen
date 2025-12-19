/*package bank_account;

import api.dtos.UserDto;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://localhost:8770").build();
    }

    public Mono<String> getUserRole(String email, String authHeader) {
        return webClient.get()
                .uri("/users/email?email={email}", email)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(UserDto.class)
                .map(UserDto::getRole)
                .onErrorReturn("ERROR");
    }
}
*/


package bank_account;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import api.dtos.UserDto;
import reactor.core.publisher.Mono;

@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(
            @Value("${users.service.url:http://localhost:8770}") String usersServiceUrl) {

        this.webClient = WebClient.builder()
                .baseUrl(usersServiceUrl)
                .build();
    }

    public Mono<String> getUserRole(String email, String authorizationHeader) {

        return webClient.get()
                .uri("/users/email?email={email}", email)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retrieve()
                .bodyToMono(UserDto.class)
                .map(UserDto::getRole)
                .onErrorReturn("ERROR");
    }
}
