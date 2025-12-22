package usersService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class WalletInitializer {

    @Autowired
    private UserRepository users;

    @Autowired
    private CryptoWalletClient walletClient;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {

        System.out.println("Initializing CRYPTO wallets for existing USERS...");

        users.findAll().forEach(u -> {

            if ("USER".equals(u.getRole())) {

                System.out.println("Creating crypto wallet for user: " + u.getEmail());

                try {
                    Boolean success = walletClient
                            .createWallet(u.getEmail())
                            .doOnError(e ->
                                    System.err.println("✗ Crypto wallet error: " + e.getMessage())
                            )
                            .block();

                    if (Boolean.TRUE.equals(success)) {
                        System.out.println("✓ Crypto wallet created for: " + u.getEmail());
                    } else {
                        System.out.println("✗ Crypto wallet NOT created for: " + u.getEmail());
                    }

                    Thread.sleep(100);

                } catch (Exception e) {
                    System.err.println("EXCEPTION creating crypto wallet: " + e.getMessage());
                }
            }

        }); 

        System.out.println("Crypto wallet initialization completed.");
    }
}
