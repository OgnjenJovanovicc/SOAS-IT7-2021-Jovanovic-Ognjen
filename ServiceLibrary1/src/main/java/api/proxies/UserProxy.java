package api.proxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import api.dtos.UserDto;

@FeignClient(name = "users-service")
//@FeignClient(name = "users-service1", url= "http://localhost:8770")
	public interface UserProxy {

	    @GetMapping("/users/email")
	    UserDto getUser(@RequestParam(value="email") String email,
	                    @RequestHeader("Authorization") String auth);
	}