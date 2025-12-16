package api.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import api.dtos.UserDto;

public interface UsersService {
    
    @GetMapping("/users")
    ResponseEntity<?> getUsers(@RequestHeader("Authorization") String authorization);
    
    @GetMapping("/users/email")
    ResponseEntity<?> getUserByEmail(@RequestParam String email, 
                                     @RequestHeader("Authorization") String authorization);
    
    @PostMapping("/users/newAdmin")
    ResponseEntity<?> createAdmin(@RequestBody UserDto dto, 
                                  @RequestHeader("Authorization") String authorization);
    
    @PostMapping("/users/newUser")
    ResponseEntity<?> createUser(@RequestBody UserDto dto, 
                                 @RequestHeader("Authorization") String authorization);

    @PutMapping("/users")
    ResponseEntity<?> updateUser(@RequestParam String email, 
                                 @RequestBody UserDto dto, 
                                 @RequestHeader("Authorization") String authorization);
    
    @DeleteMapping("/users")
    ResponseEntity<?> deleteUser(@RequestParam String email, 
                                 @RequestHeader("Authorization") String authorization);
}




