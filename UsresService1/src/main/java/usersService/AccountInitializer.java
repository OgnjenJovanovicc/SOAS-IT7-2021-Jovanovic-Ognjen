package usersService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import usersService.UserRepository;
import usersService.BankAccountClient;
import usersService.UserModel;

import java.util.List;

@Component
public class AccountInitializer {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BankAccountClient bankAccountClient;
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeBankAccounts() {
        System.out.println("Initializing bank accounts for existing USERS...");
        
    
        List<UserModel> users = userRepository.findAll();
        
        for (UserModel user : users) {
            if ("USER".equals(user.getRole())) {
                try {
                    System.out.println("Creating bank account for user: " + user.getEmail());
                    bankAccountClient.createAccountForUser(user.getEmail())
                        .subscribe(success -> {
                            if (success) {
                                System.out.println("✓ Bank account created for: " + user.getEmail());
                            } else {
                                System.out.println("✗ Failed to create bank account for: " + user.getEmail());
                            }
                        });
               
                    Thread.sleep(100);
                } catch (Exception e) {
                    System.err.println("Error creating account for " + user.getEmail() + ": " + e.getMessage());
                }
            }
        }
        System.out.println("Bank account initialization completed.");
    }
}