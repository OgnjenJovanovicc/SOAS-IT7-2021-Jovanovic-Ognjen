/*package usersService;

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
*/
package usersService;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import api.proxies.BankAccountProxy;
import api.proxies.CryptoWalletProxy;

import java.util.List;

@Component
public class AccountInitializer {

    private final UserRepository userRepository;
    private final BankAccountProxy bankAccountProxy; 
    private final CryptoWalletProxy cryptoWalletProxy; 

    public AccountInitializer(UserRepository userRepository,
                              BankAccountProxy bankAccountProxy,
                              CryptoWalletProxy cryptoWalletProxy) {
        this.userRepository = userRepository;
        this.bankAccountProxy = bankAccountProxy;
        this.cryptoWalletProxy = cryptoWalletProxy;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeAccounts() {
        System.out.println("Initializing bank accounts and crypto wallets for existing USERS...");

        List<UserModel> users = userRepository.findAll();

        for (UserModel user : users) {
            if ("USER".equals(user.getRole())) {
                try {
                    System.out.println("Creating bank account for user: " + user.getEmail());
                    bankAccountProxy.createAccountForUser(user.getEmail());
                    System.out.println("✓ Bank account created for: " + user.getEmail());

                    System.out.println("Creating crypto wallet for user: " + user.getEmail());
                    cryptoWalletProxy.createWallet(user.getEmail());
                    System.out.println("✓ Crypto wallet created for: " + user.getEmail());

                    Thread.sleep(100); 
                } catch (Exception e) {
                    System.err.println("Error creating account/wallet for " + user.getEmail() + ": " + e.getMessage());
                }
            }
        }

        System.out.println("Initialization completed.");
    }
}
