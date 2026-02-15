package usersService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@SpringBootApplication
@EnableFeignClients(basePackages = "api.proxies")
public class UsresServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UsresServiceApplication.class, args);
	}
	
}



