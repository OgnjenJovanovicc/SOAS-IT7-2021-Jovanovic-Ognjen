package usersService;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import api.proxies.CryptoWalletProxy;

@Component
public class WalletInitializer {

    private final UserRepository users;
    private final CryptoWalletProxy walletProxy; 


    public WalletInitializer(UserRepository users, CryptoWalletProxy walletProxy) {
        this.users = users;
        this.walletProxy = walletProxy;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        System.out.println("Initializing CRYPTO wallets for existing USERS...");

        users.findAll().forEach(u -> {
            if ("USER".equals(u.getRole())) {
                System.out.println("Creating crypto wallet for user: " + u.getEmail());

                try {
                    walletProxy.createWallet(u.getEmail());
                    System.out.println("✓ Crypto wallet created for: " + u.getEmail());

                    Thread.sleep(100); 

                } catch (Exception e) {
                    System.err.println("✗ Crypto wallet error for " + u.getEmail() + ": " + e.getMessage());
                }
            }
        });

        System.out.println("Crypto wallet initialization completed.");
    }
}

