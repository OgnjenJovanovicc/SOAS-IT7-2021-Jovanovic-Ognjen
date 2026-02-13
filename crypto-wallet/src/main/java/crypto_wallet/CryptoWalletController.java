package crypto_wallet;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import api.dtos.BankAccountDto;
import api.dtos.CryptoWalletDto;
import api.proxies.UserProxy;
import bank_account.BankAccountEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/crypto-wallet")
public class CryptoWalletController {

    private final CryptoWalletRepository repo;
    private final AuthDecoder auth;
    private final UserProxy users;

    public CryptoWalletController(
            CryptoWalletRepository repo,
            AuthDecoder auth,
            UserProxy users) {
        this.repo = repo;
        this.auth = auth;
        this.users = users;
    }


    @GetMapping
    public ResponseEntity<?> getAll(@RequestHeader("Authorization") String authHeader) {
        String email = auth.decodeEmailFromAuthHeader(authHeader);
        String role = users.getUser(email, authHeader).getRole();

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only ADMIN can view all wallets");
        }
/*
        List<CryptoWalletDto> result = repo.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());*/
        
        List<CryptoWalletDto>result=new ArrayList<>();
        for (CryptoWalletEntity e : repo.findAll()) {
            result.add(toDto(e));
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getOne(
            @PathVariable String email,
            @RequestHeader("Authorization") String authHeader) {

        String requesterEmail = auth.decodeEmailFromAuthHeader(authHeader);
        String role = users.getUser(requesterEmail, authHeader).getRole();

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
        String role = users.getUser(requesterEmail, authHeader).getRole();

        if (!"USER".equals(role) || !email.equals(requesterEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only USER can exchange own crypto");
        }

        final String from = fromCrypto.toUpperCase();
        final String to = toCrypto.toUpperCase();

        return repo.findByEmail(email).map(wallet -> {

            BigDecimal currentFromAmount = getAmountByCrypto(wallet, from);
            if (currentFromAmount.compareTo(amount) < 0) {
                return ResponseEntity.badRequest()
                        .body("Insufficient " + from + " funds. Available: " + currentFromAmount);
            }

            BigDecimal newFromAmount = currentFromAmount.subtract(amount);
            setAmountByCrypto(wallet, from, newFromAmount);

            BigDecimal converted = amount.multiply(exchangeRate);

            BigDecimal currentToAmount = getAmountByCrypto(wallet, to);
            BigDecimal newToAmount = currentToAmount.add(converted);
            setAmountByCrypto(wallet, to, newToAmount);

            repo.save(wallet);

            CryptoWalletDto walletDto = toDto(wallet);
            
            Map<String, Object> response = new HashMap<>();
            response.put("wallet", walletDto);
            response.put("transaction", Map.of(
                "from", from,
                "to", to,
                "amount", amount,
                "exchangeRate", exchangeRate,
                "convertedAmount", converted,
                "newFromBalance", newFromAmount,
                "newToBalance", newToAmount,
                "message", String.format("Exchanged %s %s for %s %s", 
                    amount, from, converted, to)
            ));
            
            return ResponseEntity.ok(response);

        }).orElse(ResponseEntity.notFound().build());
    }

    private BigDecimal getAmountByCrypto(CryptoWalletEntity wallet, String crypto) {
        return switch (crypto.toUpperCase()) {
            case "BTC" -> wallet.getBtcAmount();
            case "ETH" -> wallet.getEthAmount();
            case "USDT" -> wallet.getUsdtAmount();
            default -> BigDecimal.ZERO;
        };
    }

    private void setAmountByCrypto(CryptoWalletEntity wallet, String crypto, BigDecimal amount) {
        switch (crypto.toUpperCase()) {
            case "BTC" -> wallet.setBtcAmount(amount);
            case "ETH" -> wallet.setEthAmount(amount);
            case "USDT" -> wallet.setUsdtAmount(amount);
            default -> throw new IllegalArgumentException("Unsupported crypto: " + crypto);
        }
    }

    @PutMapping("/{email}")
    public ResponseEntity<?> updateWallet(
            @PathVariable String email,
            @RequestBody CryptoWalletDto dto,
            @RequestHeader("Authorization") String authHeader) {

        String admin = auth.decodeEmailFromAuthHeader(authHeader);
        String role = users.getUser(admin, authHeader).getRole();

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
        String role = users.getUser(admin, authHeader).getRole();

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
    
    @PostMapping("/{email}/withdraw")
    public ResponseEntity<?> withdraw(
            @PathVariable String email,
            @RequestParam String currency,
            @RequestParam BigDecimal amount,
            @RequestHeader("Authorization") String authHeader) {

        String requester = auth.decodeEmailFromAuthHeader(authHeader);
        //String role = users.getUserRole(requester, authHeader).block();
        String role = users.getUser(requester, authHeader).getRole();
        if (!"USER".equals(role) || !email.equals(requester)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only USER can withdraw from own wallet");
        }

        CryptoWalletEntity wallet = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        currency = currency.toUpperCase();
        
        BigDecimal currentAmount = getAmountByCrypto(wallet, currency);
        if (currentAmount.compareTo(amount) < 0) {
            return ResponseEntity.badRequest()
                    .body("Insufficient " + currency + ". Available: " + currentAmount);
        }
        
        BigDecimal newAmount = currentAmount.subtract(amount);
        setAmountByCrypto(wallet, currency, newAmount);

        repo.save(wallet);
        
        Map<String, Object> response = new HashMap<>();
        response.put("wallet", toDto(wallet));
        response.put("withdrawal", Map.of(
            "currency", currency,
            "amount", amount,
            "newBalance", newAmount,
            "message", "Withdrawn " + amount + " " + currency
        ));
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{email}/deposit")
    public ResponseEntity<?> deposit(
            @PathVariable String email,
            @RequestParam String currency,
            @RequestParam BigDecimal amount,
            @RequestHeader("Authorization") String authHeader) {

        String requester = auth.decodeEmailFromAuthHeader(authHeader);
        String role = users.getUser(requester, authHeader).getRole();

        if (!"USER".equals(role) || !email.equals(requester)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only USER can deposit to own wallet");
        }

        CryptoWalletEntity wallet = repo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));

        currency = currency.toUpperCase();
        BigDecimal currentAmount = getAmountByCrypto(wallet, currency);
        BigDecimal newAmount = currentAmount.add(amount);
        
        setAmountByCrypto(wallet, currency, newAmount);

        repo.save(wallet);
        
        Map<String, Object> response = new HashMap<>();
        response.put("wallet", toDto(wallet));
        response.put("deposit", Map.of(
            "currency", currency,
            "amount", amount,
            "newBalance", newAmount,
            "message", "Deposited " + amount + " " + currency
        ));
        
        return ResponseEntity.ok(response);
    }
}