package crypto_exchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
	    "crypto_exchange",
	    "api.services",
	    "api.dtos",
	    "util.exceptions"
	})
public class CryptoWallet1Application {

	public static void main(String[] args) {
		SpringApplication.run(CryptoWallet1Application.class, args);
	}

}
