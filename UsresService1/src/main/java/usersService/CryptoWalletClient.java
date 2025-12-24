package usersService;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import api.dtos.UserDto;
import reactor.core.publisher.Mono;

@Component
public class CryptoWalletClient {

    private final WebClient webClient;
    private final RestTemplate restTemplate = new RestTemplate();

    public CryptoWalletClient(
            @Value("${crypto.wallet.service.url:http://localhost:8300}") String baseUrl) {

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public Mono<Boolean> createWallet(String email) {
        System.out.println("→ Calling crypto-wallet service for: " + email);

        return webClient.post()
                .uri("/crypto-wallet/internal/create/{email}", email)
                .retrieve()
                .bodyToMono(Void.class)
                .map(v -> true)
                .doOnSuccess(v -> System.out.println("✓ Crypto wallet HTTP OK for: " + email))
                .doOnError(e -> System.err.println("✗ Crypto wallet HTTP ERROR for: " + email))
                .onErrorReturn(false);
    }
    public UserDto getUserByEmail(String email, String authHeader) {
        return restTemplate.getForObject(
            "http://localhost:8200/users/by-email?email={email}",
            UserDto.class,
            email
        );
    }

    public void updateWallet(Map<String, BigDecimal> wallet) {
        restTemplate.put(
            "http://localhost:8300/wallet",
            wallet
        );
    }

    public Mono<Boolean> deleteWallet(String email) {
        return webClient.delete()
                .uri("/crypto-wallet/internal/delete/{email}", email)
                .retrieve()
                .bodyToMono(Void.class)
                .map(v -> true)
                .onErrorReturn(false);
    }
}