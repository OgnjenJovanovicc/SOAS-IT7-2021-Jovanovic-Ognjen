package api.proxies;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "crypto-wallet", url = "http://localhost:8300")
public interface CryptoWalletProxy {

    @PostMapping("/crypto-wallet/{email}/exchange")
    Map<String, Object> exchange(
            @PathVariable String email,
            @RequestParam String fromCrypto,
            @RequestParam String toCrypto,
            @RequestParam BigDecimal amount,
            @RequestParam BigDecimal exchangeRate,
            @RequestHeader("Authorization") String auth);
    		
    /* ========= WITHDRAW CRYPTO ========= */
    @PostMapping("/crypto-wallet/{email}/withdraw")
    ResponseEntity<?> withdraw(
            @PathVariable String email,
            @RequestParam String currency,
            @RequestParam BigDecimal amount,
            @RequestHeader("Authorization") String authHeader
    );

    /* ========= DEPOSIT CRYPTO ========= */
    @PostMapping("/crypto-wallet/{email}/deposit")
    Map<String, Object> deposit(
            @PathVariable String email,
            @RequestParam String currency,
            @RequestParam BigDecimal amount,
            @RequestHeader("Authorization") String authHeader
    );
    @PostMapping("/crypto-wallet/{email}/trade-fiat-to-crypto")
    Map<String, Object> tradeFiatToCrypto(
            @PathVariable String email,
            @RequestParam String fiatCurrency,  
            @RequestParam String cryptoCurrency,
            @RequestParam BigDecimal fiatAmount,
            @RequestParam BigDecimal exchangeRate,
            @RequestHeader("Authorization") String auth);

}