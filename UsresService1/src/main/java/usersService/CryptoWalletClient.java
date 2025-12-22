package usersService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class CryptoWalletClient {

    private final WebClient webClient;

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
    

    public Mono<Boolean> deleteWallet(String email) {
        return webClient.delete()
                .uri("/crypto-wallet/internal/delete/{email}", email)
                .retrieve()
                .bodyToMono(Void.class)
                .map(v -> true)
                .onErrorReturn(false);
    }
}