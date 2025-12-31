package usersService;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import api.dtos.UserDto;
import api.services.UsersService;
import usersService.decoder.Decoder;

@RestController
@RequestMapping("/users")
public class UserServiceImpl implements UsersService {

    @Autowired
    private UserRepository repo;
    
    @Autowired
    private Decoder decoder;

    
    @Autowired
    private BankAccountClient bankAccountClient;
    
    @Autowired
    private CryptoWalletClient cryptoWalletClient;

    
    @Override
    @GetMapping
    public ResponseEntity<?> getUsers(@RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null) {
            System.out.print("Authorization header missing");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization header required");
        }
        
        String email = decoder.decodeHeader(authorization);
        UserModel requester = repo.findByEmail(email);
        
        if (requester == null || (!requester.getRole().equals("OWNER") && !requester.getRole().equals("ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only ADMIN and OWNER cau use this service");
        }
        
        List<UserDto> dtos = new ArrayList<>();
        for (UserModel user : repo.findAll()) {
            dtos.add(convertModelToDto(user));
        }
        return ResponseEntity.ok(dtos);
    }
   
    @Override
    @GetMapping("/email")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email,
                                           @RequestHeader(value = "Authorization", required = false) String authorization) {

        UserModel target = repo.findByEmail(email);
        if (target == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        return ResponseEntity.ok(convertModelToDto(target));
    }
 /*   @GetMapping("/auth/{email}")
    public ResponseEntity<?> getUserForAuth(@PathVariable String email) {
        UserModel user = repo.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        return ResponseEntity.ok(convertModelToDto(user));
    }*/
    
    @GetMapping("/auth")
    public ResponseEntity<?> getUserForAuth(@RequestParam String email) {
        UserModel user = repo.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        return ResponseEntity.ok(convertModelToDto(user));
    }


    @Override
    @PostMapping("/newAdmin")
    public ResponseEntity<?> createAdmin(@RequestBody UserDto dto,
                                         @RequestHeader("Authorization") String authorization) {
        String email = decoder.decodeHeader(authorization);
        UserModel requester = repo.findByEmail(email);
        
        if (requester == null || !requester.getRole().equals("OWNER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only OWNER can create ADMIN");
        }

        if (repo.findByEmail(dto.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User with this email already exists");
        }
        
        dto.setRole("ADMIN");
        UserModel model = convertDtoToModel(dto);
        repo.save(model);

        return ResponseEntity.status(HttpStatus.CREATED).body(convertModelToDto(model));
    }
   
    @Override
    @PostMapping("/newUser")
    public ResponseEntity<?> createUser(@RequestBody UserDto dto,
                                        @RequestHeader("Authorization") String authorization) {
        String email = decoder.decodeHeader(authorization);
        UserModel requester = repo.findByEmail(email);

        if (requester == null || (!requester.getRole().equals("OWNER") && !requester.getRole().equals("ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        if (repo.findByEmail(dto.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User with this email already exists");
        }

        dto.setRole("USER");
        UserModel model = convertDtoToModel(dto);
        repo.save(model);
        cryptoWalletClient.createWallet(dto.getEmail())
        .subscribe(success -> {
            if (success) {
                System.out.println("Crypto wallet created for user: " + dto.getEmail());
            } else {
                System.out.println("Failed to create crypto wallet for user: " + dto.getEmail());
            }
        });
        
        try {
            bankAccountClient.createAccountForUser(dto.getEmail())
                .subscribe(success -> {
                    if (success) {
                        System.out.println("Bank account created for user: " + dto.getEmail());
                    } else {
                        System.out.println("Failed to create bank account for user: " + dto.getEmail());
                    }
                });
        } catch (Exception e) {
            System.err.println("Error creating bank account: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(convertModelToDto(model));
    }

    @Override
    @PutMapping
    public ResponseEntity<?> updateUser(@RequestParam String email,
                                        @RequestBody UserDto dto,
                                        @RequestHeader("Authorization") String authorization) {

        String requesterEmail = decoder.decodeHeader(authorization);
        UserModel requester = repo.findByEmail(requesterEmail);

        if (requester == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        UserModel target = repo.findByEmail(email);
        if (target == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        if (requester.getRole().equals("USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("USER cannot update users");
        }

        if (requester.getRole().equals("ADMIN") && !target.getRole().equals("USER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("ADMIN can update only USERs");
        }

    
        if (dto.getRole() != null && !dto.getRole().equals(target.getRole())) {

            if (target.getRole().equals("USER") && !dto.getRole().equals("USER")) {
                try {
                    bankAccountClient.deleteAccountForUser(email)
                        .subscribe(success -> {
                            if (success) {
                                System.out.println("Bank account deleted for user (role change): " + email);
                            }
                        });
                } catch (Exception e) {
                    System.err.println("Error deleting bank account during role change: " + e.getMessage());
                }
            }
            
            if (!target.getRole().equals("USER") && dto.getRole().equals("USER")) {
                try {
                    bankAccountClient.createAccountForUser(email)
                        .subscribe(success -> {
                            if (success) {
                                System.out.println("Bank account created for user (role change to USER): " + email);
                            }
                        });
                } catch (Exception e) {
                    System.err.println("Error creating bank account during role change: " + e.getMessage());
                }
            }
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            target.setPassword(dto.getPassword());
        }

        if (requester.getRole().equals("OWNER")) {
            if (dto.getRole() != null && !dto.getRole().equals(target.getRole())) {
                if (dto.getRole().equals("OWNER")
                        && repo.findByRole("OWNER") != null
                        && !target.getRole().equals("OWNER")) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("There can be only one OWNER");
                }
                target.setRole(dto.getRole());
            }
        }

        repo.save(target);
        return ResponseEntity.ok(convertModelToDto(target));
    }

    
    @Override
    @DeleteMapping
    public ResponseEntity<?> deleteUser(@RequestParam String email,
                                        @RequestHeader("Authorization") String authorization) {
        String requesterEmail = decoder.decodeHeader(authorization);
        UserModel requester = repo.findByEmail(requesterEmail);
        
        if (requester == null || !requester.getRole().equals("OWNER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only OWNER can delete users");
        }

        UserModel target = repo.findByEmail(email);
        if (target == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        
        if (target.getEmail().equals(requester.getEmail())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("OWNER cannot delete themselves");
        }
        cryptoWalletClient.deleteWallet(email)
        .subscribe(success -> {
            if (success) {
                System.out.println("Crypto wallet deleted for user: " + email);
            }
        });
 
        if (target.getRole().equals("USER")) {
            try {
                boolean deleted = bankAccountClient.deleteAccountForUser(email).block();
                if (deleted) {
                    System.out.println("Bank account deleted for user: " + email);
                } else {
                    System.out.println("No bank account found for user: " + email);
                }
            } catch (Exception e) {
                System.err.println("Error deleting bank account: " + e.getMessage());

            }
        }
        
        repo.delete(target);
        return ResponseEntity.ok("User deleted");
    }
    
    private UserDto convertModelToDto(UserModel model) {
        return new UserDto(model.getEmail(), model.getPassword(), model.getRole());
    }

    private UserModel convertDtoToModel(UserDto dto) {
        return new UserModel(dto.getEmail(), dto.getPassword(), dto.getRole());
    }

    
}