package api.proxies;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

//@FeignClient(name = "bank-account", url = "http://localhost:8200")
@FeignClient(name = "bank-account")
public interface BankAccountProxy {

    @PostMapping("/bank-account/{email}/exchange")
    Map<String, Object> exchange(
            @PathVariable(value="email") String email,
            @RequestParam(value="fromCurrency") String fromCurrency,
            @RequestParam(value="toCurrency") String toCurrency,
            @RequestParam(value="amount") BigDecimal amount,
            @RequestParam(value="exchangeRate") BigDecimal exchangeRate,
            @RequestHeader("Authorization") String auth);
    
    @PostMapping("/bank-account/{email}/trade-fiat-to-fiat")
    Map<String, Object> tradeFiatToFiat(
            @PathVariable(value="email") String email,
            @RequestParam(value="fromCurrency") String fromCurrency,
            @RequestParam(value="toCurrency") String toCurrency,
            @RequestParam(value="amount") BigDecimal amount,
            @RequestParam(value="exchangeRate") BigDecimal exchangeRate,
            @RequestHeader("Authorization") String auth);
    
    @PostMapping("/bank-account/{email}/withdraw")
    Map<String, Object> withdraw(
            @PathVariable(value="email") String email,
            @RequestParam (value="currency")String currency,
            @RequestParam(value="amount") BigDecimal amount,
            @RequestHeader("Authorization") String auth
    );

    @PostMapping("/bank-account/{email}/deposit")
    Map<String, Object> deposit(
            @PathVariable(value="email") String email,
            @RequestParam (value="currency")String currency,
            @RequestParam (value="amount")BigDecimal amount,
            @RequestHeader("Authorization") String auth
    );
    
    @PostMapping("/bank-account/internal/create/{email}")
    void createAccountForUser(@PathVariable(value="email") String email);

    @DeleteMapping("/bank-account/internal/delete/{email}")
    void deleteAccountForUser(@PathVariable(value="email") String email);
}