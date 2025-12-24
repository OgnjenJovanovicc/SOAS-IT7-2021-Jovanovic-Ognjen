package crypto_conversion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "crypto_conversion",
        "util.exceptions",
        "api.services"
})
@EnableFeignClients(basePackages = "api.proxies")
public class CryptoConversionApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptoConversionApplication.class, args);
	}

}
