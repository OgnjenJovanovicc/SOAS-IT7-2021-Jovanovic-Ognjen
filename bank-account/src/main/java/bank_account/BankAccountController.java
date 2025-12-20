package bank_account;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import api.dtos.BankAccountDto;
import api.dtos.CreateBankAccountDto;

import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/bank-account")
public class BankAccountController {

    @Autowired
    private BankAccountRepository repo;

    @Autowired
    private UserServiceClient users;

    @Autowired
    private AuthDecoder auth;

    private BankAccountEntity convertToEntity(BankAccountDto dto) {
        BankAccountEntity e = new BankAccountEntity();
        e.setEmail(dto.getEmail());
        e.setUsdAmount(dto.getUsdAmount() != null ? dto.getUsdAmount() : BigDecimal.ZERO);
        e.setEurAmount(dto.getEurAmount() != null ? dto.getEurAmount() : BigDecimal.ZERO);
        e.setGbpAmount(dto.getGbpAmount() != null ? dto.getGbpAmount() : BigDecimal.ZERO);
        e.setChfAmount(dto.getChfAmount() != null ? dto.getChfAmount() : BigDecimal.ZERO);
        e.setRsdAmount(dto.getRsdAmount() != null ? dto.getRsdAmount() : BigDecimal.ZERO);
        return e;
    }

    private BankAccountDto convertToDto(BankAccountEntity e) {
        BankAccountDto dto = new BankAccountDto();
        dto.setEmail(e.getEmail());
        dto.setUsdAmount(e.getUsdAmount());
        dto.setEurAmount(e.getEurAmount());
        dto.setGbpAmount(e.getGbpAmount());
        dto.setChfAmount(e.getChfAmount());
        dto.setRsdAmount(e.getRsdAmount());
        return dto;
    }

    
    
    @GetMapping
    public ResponseEntity<?> getAll(@RequestHeader("Authorization") String authHeader) {

        String requester = auth.decodeEmailFromAuthHeader(authHeader);
        String role = users.getUserRole(requester, authHeader).block();

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only ADMIN can view all accounts");
        }

        List<BankAccountDto> result = new ArrayList<>();
        for (BankAccountEntity e : repo.findAll()) {
            result.add(convertToDto(e));
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getOne(@PathVariable String email,
                                    @RequestHeader("Authorization") String authHeader) {

     
        String requester = auth.decodeEmailFromAuthHeader(authHeader);

        String role = users.getUserRole(requester, authHeader).block();

        if (!"USER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only USER role can access this endpoint");
        }

        if (!requester.equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("USER can view only their own account");
        }

        BankAccountEntity entity = repo.findByEmail(email).orElse(null);

        if (entity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Account not found");
        }

        return ResponseEntity.ok(convertToDto(entity));
    }


    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateBankAccountDto dto,
                                    @RequestHeader("Authorization") String authHeader) {

        String requester = auth.decodeEmailFromAuthHeader(authHeader);
        String role = users.getUserRole(requester, authHeader).block();

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only ADMIN can create accounts");
        }

        if (repo.existsByEmail(dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Account already exists");
        }

        BankAccountEntity entity = new BankAccountEntity();
        entity.setEmail(dto.getEmail());
        entity.setUsdAmount(BigDecimal.ZERO);
        entity.setEurAmount(BigDecimal.ZERO);
        entity.setGbpAmount(BigDecimal.ZERO);
        entity.setChfAmount(BigDecimal.ZERO);
        entity.setRsdAmount(BigDecimal.ZERO);

        repo.save(entity);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(convertToDto(entity));
    }

    @PostMapping("/internal/create/{email}")
    public ResponseEntity<?> internalCreate(@PathVariable String email) {

        if (!repo.existsByEmail(email)) {
            BankAccountEntity entity = new BankAccountEntity();
            entity.setEmail(email);
            entity.setUsdAmount(BigDecimal.ZERO);
            entity.setEurAmount(BigDecimal.ZERO);
            entity.setGbpAmount(BigDecimal.ZERO);
            entity.setChfAmount(BigDecimal.ZERO);
            entity.setRsdAmount(BigDecimal.ZERO);
            repo.save(entity);
        }

        return ResponseEntity.ok("Account created for " + email);
    }

    @DeleteMapping("/internal/delete/{email}")
    public ResponseEntity<?> internalDelete(@PathVariable String email) {

        repo.deleteByEmail(email);
        return ResponseEntity.ok("Account deleted for " + email);
    }
    
    @PutMapping("/{email}")
    public ResponseEntity<?> update(@PathVariable String email,
                                    @RequestBody BankAccountDto dto,
                                    @RequestHeader("Authorization") String authHeader) {

        String requester = auth.decodeEmailFromAuthHeader(authHeader);
        String role = users.getUserRole(requester, authHeader).block();

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only ADMIN can update bank accounts");
        }

        BankAccountEntity entity = repo.findByEmail(email).orElse(null);

        if (entity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Account not found");
        }

        entity.setUsdAmount(dto.getUsdAmount() != null ? dto.getUsdAmount() : entity.getUsdAmount());
        entity.setEurAmount(dto.getEurAmount() != null ? dto.getEurAmount() : entity.getEurAmount());
        entity.setGbpAmount(dto.getGbpAmount() != null ? dto.getGbpAmount() : entity.getGbpAmount());
        entity.setChfAmount(dto.getChfAmount() != null ? dto.getChfAmount() : entity.getChfAmount());
        entity.setRsdAmount(dto.getRsdAmount() != null ? dto.getRsdAmount() : entity.getRsdAmount());

        repo.save(entity);

        return ResponseEntity.ok(convertToDto(entity));
    
    }
    

    @DeleteMapping("/{email}")
    public ResponseEntity<?> delete(@PathVariable String email,
                                    @RequestHeader("Authorization") String authHeader) {

        String requester = auth.decodeEmailFromAuthHeader(authHeader);
        String role = users.getUserRole(requester, authHeader).block();

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only ADMIN can delete bank accounts");
        }

        if (!repo.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Account not found");
        }

        repo.deleteByEmail(email);

        return ResponseEntity.ok("Account deleted for " + email);
    }
    
    @PutMapping("/internal/update/{email}")
    public ResponseEntity<?> internalUpdate(
            @PathVariable String email, 
            @RequestBody BankAccountDto dto) {
        
        BankAccountEntity entity = repo.findByEmail(email).orElse(null);
        if (entity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Account not found");
        }
        
        // Ažuriraj iznose
        if (dto.getUsdAmount() != null) entity.setUsdAmount(dto.getUsdAmount());
        if (dto.getEurAmount() != null) entity.setEurAmount(dto.getEurAmount());
        if (dto.getGbpAmount() != null) entity.setGbpAmount(dto.getGbpAmount());
        if (dto.getChfAmount() != null) entity.setChfAmount(dto.getChfAmount());
        if (dto.getRsdAmount() != null) entity.setRsdAmount(dto.getRsdAmount());
        
        repo.save(entity);
        return ResponseEntity.ok(convertToDto(entity));
    }
    
    @PostMapping("/{email}/exchange")
    public ResponseEntity<?> exchangeCurrency(
            @PathVariable String email,
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency,
            @RequestParam BigDecimal amount,
            @RequestParam BigDecimal exchangeRate,
            @RequestHeader("Authorization") String authHeader) {
        
        // 1. Provera ko je pozivaoc
        String requester = auth.decodeEmailFromAuthHeader(authHeader);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        
        // 2. Provera role - samo USER može (ili ADMIN)
        String role = users.getUserRole(requester, authHeader).block();
        if (!"USER".equals(role) && !"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only USER or ADMIN can exchange currency");
        }
        
        // 3. USER može samo svoj račun
        if ("USER".equals(role) && !requester.equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("USER can exchange only on own account");
        }
        
        // 4. Pronađi račun
        BankAccountEntity entity = repo.findByEmail(email).orElse(null);
        if (entity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
        }
        
        // 5. Provera sredstava
        BigDecimal currentAmount = entity.getAmountByCurrency(fromCurrency);
        if (currentAmount.compareTo(amount) < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Insufficient funds in " + fromCurrency);
        }
        
        // 6. Izvrši razmenu
        BigDecimal convertedAmount = amount.multiply(exchangeRate);
        BigDecimal newFromAmount = currentAmount.subtract(amount);
        BigDecimal currentToAmount = entity.getAmountByCurrency(toCurrency);
        BigDecimal newToAmount = currentToAmount.add(convertedAmount);
        
        entity.setAmountByCurrency(fromCurrency, newFromAmount);
        entity.setAmountByCurrency(toCurrency, newToAmount);
        
        repo.save(entity);
        
        // 7. Vrati odgovor
        Map<String, Object> response = new HashMap<>();
        response.put("email", email);
        response.put("transactionMessage", 
            String.format("Uspešno je izvršena razmena %s: %s za %s: %s",
                fromCurrency, amount, toCurrency, convertedAmount));
        response.put("newBalances", convertToDto(entity));
        
        return ResponseEntity.ok(response);
    }

}
