package crypto_wallet;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import api.dtos.CryptoWalletDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/crypto-wallet")
public class CryptoWalletController {

    private final CryptoWalletRepository repo;
    private final AuthDecoder auth;
    private final UserSerivceClient users;

    public CryptoWalletController(
            CryptoWalletRepository repo,
            AuthDecoder auth,
            UserSerivceClient users) {
        this.repo = repo;
        this.auth = auth;
        this.users = users;
    }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestHeader("Authorization") String authHeader) {

        String email = auth.decodeEmailFromAuthHeader(authHeader);
        String role = users.getUserRole(email, authHeader).block();

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only ADMIN can view all wallets");
        }

        List<CryptoWalletDto> result = repo.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getOne(
            @PathVariable String email,
            @RequestHeader("Authorization") String authHeader) {

        String requesterEmail = auth.decodeEmailFromAuthHeader(authHeader);
        String role = users.getUserRole(requesterEmail, authHeader).block();

        if ("OWNER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("OWNER is not allowed to access crypto wallets");
        }
        
        if ("ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("ADMIN is not allowed to access crypto wallets");
        }

        if ("USER".equals(role)) {
            return repo.findByEmail(requesterEmail)
                    .map(w -> ResponseEntity.ok(toDto(w)))
                    .orElse(ResponseEntity.notFound().build());
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
 
    @PostMapping("/{email}/exchange")
    public ResponseEntity<?> exchange(
            @PathVariable String email,
            @RequestParam String fromCrypto,
            @RequestParam String toCrypto,
            @RequestParam BigDecimal amount,
            @RequestParam BigDecimal exchangeRate,
            @RequestHeader("Authorization") String authHeader) {

        String requesterEmail = auth.decodeEmailFromAuthHeader(authHeader);
        String role = users.getUserRole(requesterEmail, authHeader).block();

        if (!"USER".equals(role) || !email.equals(requesterEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only USER can exchange own crypto");
        }

        return repo.findByEmail(email).map(wallet -> {

            if ("BTC".equals(fromCrypto) &&
                wallet.getBtcAmount().compareTo(amount) < 0) {
                return ResponseEntity.badRequest().body("Insufficient BTC funds");
            }

            if ("BTC".equals(fromCrypto)) {
                wallet.setBtcAmount(
                    wallet.getBtcAmount().subtract(amount)
                );
            }

            BigDecimal converted = amount.multiply(exchangeRate);

            if ("USDT".equals(toCrypto)) {
                wallet.setUsdtAmount(
                    wallet.getUsdtAmount().add(converted)
                );
            }

            repo.save(wallet);
            return ResponseEntity.ok(toDto(wallet));

        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{email}")
    public ResponseEntity<?> updateWallet(
            @PathVariable String email,
            @RequestBody CryptoWalletDto dto,
            @RequestHeader("Authorization") String authHeader) {

        String admin = auth.decodeEmailFromAuthHeader(authHeader);
        String role = users.getUserRole(admin, authHeader).block();

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only ADMIN can update wallets");
        }

        return repo.findByEmail(email)
                .map(wallet -> {

                    if (dto.getBtcAmount() != null) {
                        wallet.setBtcAmount(dto.getBtcAmount());
                    }
                    if (dto.getEthAmount() != null) {
                        wallet.setEthAmount(dto.getEthAmount());
                    }
                    if (dto.getUsdtAmount() != null) {
                        wallet.setUsdtAmount(dto.getUsdtAmount());
                    }

                    repo.save(wallet);
                    return ResponseEntity.ok(toDto(wallet));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<?> delete(
            @PathVariable String email,
            @RequestHeader("Authorization") String authHeader) {

        String admin = auth.decodeEmailFromAuthHeader(authHeader);
        String role = users.getUserRole(admin, authHeader).block();

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        repo.deleteByEmail(email);
        return ResponseEntity.ok("Wallet deleted");
    }

    private CryptoWalletDto toDto(CryptoWalletEntity e) {
        CryptoWalletDto dto = new CryptoWalletDto();
        dto.setEmail(e.getEmail());
        dto.setBtcAmount(e.getBtcAmount());
        dto.setEthAmount(e.getEthAmount());
        dto.setUsdtAmount(e.getUsdtAmount());
        return dto;
    }
    

    @PostMapping("/internal/create/{email}")
    public ResponseEntity<Void> internalCreate(@PathVariable String email) {


        if (repo.existsByEmail(email)) {
            return ResponseEntity.ok().build();
        }

        CryptoWalletEntity wallet = new CryptoWalletEntity(email);
        repo.save(wallet);

        System.out.println("→ Internal wallet created for: " + email);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/internal/delete/{email}")
    public ResponseEntity<Void> internalDelete(@PathVariable String email) {

        repo.findByEmail(email).ifPresent(repo::delete);

        System.out.println("→ Internal wallet deleted for: " + email);
        return ResponseEntity.ok().build();
    }

}
