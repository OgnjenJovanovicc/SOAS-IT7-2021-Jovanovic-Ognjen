package api.services;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import api.dtos.UserDto;

public interface UsersService {
	
	@GetMapping("/users")
	List<UserDto> getUsers();
	
	@GetMapping("/users/email")
	UserDto getUserByEmail(@RequestParam String email);
	
	@PostMapping("/users/newAdmin")
	ResponseEntity<?>createAdmin(@RequestBody UserDto dto);
	
	@PostMapping("/users/newUser")
	ResponseEntity<?>createUser(@RequestBody UserDto dto);

	@PutMapping("/users")
	ResponseEntity<?>updateUser(@RequestBody UserDto dto);
}
