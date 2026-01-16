package api.proxies;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

//@FeignClient(name = "crypto-wallet", url = "http://localhost:8300")
@FeignClient(name = "crypto-wallet")
public interface CryptoWalletProxy {

    @PostMapping("/crypto-wallet/{email}/exchange")
    Map<String, Object> exchange(
            @PathVariable (value="email") String email,
            @RequestParam(value="fromCrypto") String fromCrypto,
            @RequestParam (value="toCrypto")String toCrypto,
            @RequestParam(value="amount") BigDecimal amount,
            @RequestParam (value="exchangeRate")BigDecimal exchangeRate,
            @RequestHeader("Authorization") String auth);
    		
    /* ========= WITHDRAW CRYPTO ========= */
    @PostMapping("/crypto-wallet/{email}/withdraw")
    ResponseEntity<?> withdraw(
            @PathVariable(value="email") String email,
            @RequestParam(value="currency") String currency,
            @RequestParam(value="amount") BigDecimal amount,
            @RequestHeader("Authorization") String authHeader
    );

    /* ========= DEPOSIT CRYPTO ========= */
    @PostMapping("/crypto-wallet/{email}/deposit")
    Map<String, Object> deposit(
            @PathVariable(value="email") String email,
            @RequestParam(value="currency") String currency,
            @RequestParam(value="amount") BigDecimal amount,
            @RequestHeader("Authorization") String authHeader
    );
    @PostMapping("/crypto-wallet/{email}/trade-fiat-to-crypto")
    Map<String, Object> tradeFiatToCrypto(
            @PathVariable(value="email") String email,
            @RequestParam (value="fiatCurrency")String fiatCurrency,  
            @RequestParam(value="cryptoCurrency") String cryptoCurrency,
            @RequestParam(value="fiatAmount") BigDecimal fiatAmount,
            @RequestParam(value="exchangeRate") BigDecimal exchangeRate,
            @RequestHeader("Authorization") String auth);
    
    @PostMapping("/crypto-wallet/internal/create/{email}")
    void createWallet(@PathVariable("email") String email);

    @DeleteMapping("/crypto-wallet/internal/delete/{email}")
    void deleteWallet(@PathVariable("email") String email);

}