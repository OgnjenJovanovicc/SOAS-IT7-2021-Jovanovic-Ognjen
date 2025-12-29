package api.proxies;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "bank-account", url = "http://localhost:8200")
public interface BankAccountProxy {

    @PostMapping("/bank-account/{email}/exchange")
    Map<String, Object> exchange(
            @PathVariable String email,
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency,
            @RequestParam BigDecimal amount,
            @RequestParam BigDecimal exchangeRate,
            @RequestHeader("Authorization") String auth);
    
    @PostMapping("/bank-account/{email}/trade-fiat-to-fiat")
    Map<String, Object> tradeFiatToFiat(
            @PathVariable String email,
            @RequestParam String fromCurrency,
            @RequestParam String toCurrency,
            @RequestParam BigDecimal amount,
            @RequestParam BigDecimal exchangeRate,
            @RequestHeader("Authorization") String auth);
    
    @PostMapping("/bank-account/{email}/withdraw")
    Map<String, Object> withdraw(
            @PathVariable String email,
            @RequestParam String currency,
            @RequestParam BigDecimal amount,
            @RequestHeader("Authorization") String auth
    );

    @PostMapping("/bank-account/{email}/deposit")
    Map<String, Object> deposit(
            @PathVariable String email,
            @RequestParam String currency,
            @RequestParam BigDecimal amount,
            @RequestHeader("Authorization") String auth
    );
}
