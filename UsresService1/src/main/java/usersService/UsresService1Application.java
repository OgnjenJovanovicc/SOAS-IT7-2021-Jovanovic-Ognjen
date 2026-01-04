package usersService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@SpringBootApplication
@EnableFeignClients(basePackages = "api.proxies")
public class UsresService1Application {

	public static void main(String[] args) {
		SpringApplication.run(UsresService1Application.class, args);
	}
	
}



