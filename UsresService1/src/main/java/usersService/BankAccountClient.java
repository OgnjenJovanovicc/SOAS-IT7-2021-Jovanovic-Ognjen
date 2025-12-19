package usersService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

@Component
public class BankAccountClient {
	
	private final WebClient webClient;


	 public BankAccountClient(@Value("${bank.account.service.url:http://localhost:8200}") String bankAccountServiceUrl) {
	        this.webClient = WebClient.builder()
	                .baseUrl(bankAccountServiceUrl)
	                .build();
	    }
	 
	  public Mono<Boolean> createAccountForUser(String email) {
	        return webClient.post()
	                .uri("/bank-account/internal/create/{email}", email)
	                .retrieve()
	                .toBodilessEntity()
	                .map(response -> response.getStatusCode() == HttpStatus.OK)
	                .onErrorReturn(false);
	    }
	 
	  
	  public Mono<Boolean> deleteAccountForUser(String email) {
	        return webClient.delete()
	                .uri("/bank-account/internal/delete/{email}", email)
	                .retrieve()
	                .toBodilessEntity()
	                .map(response -> response.getStatusCode() == HttpStatus.OK)
	                .onErrorReturn(false);
	    }
}
