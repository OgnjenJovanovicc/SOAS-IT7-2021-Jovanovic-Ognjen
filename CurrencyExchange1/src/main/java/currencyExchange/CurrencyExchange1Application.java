package currencyExchange;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages= {"util.exceptions", "currencyExchange"})
public class CurrencyExchange1Application {

	public static void main(String[] args) {
		SpringApplication.run(CurrencyExchange1Application.class, args);
	}

}
