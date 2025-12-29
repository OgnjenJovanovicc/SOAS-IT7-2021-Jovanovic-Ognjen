package api.services;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import api.dtos.BankAccountDto;
import api.dtos.CreateBankAccountDto;


public interface BankAccountService {
	
	
	    ResponseEntity<?> getAllAccounts(@RequestHeader("Authorization") String authorization);
	    
	    @GetMapping("/bank-account/{email}")
	    ResponseEntity<?> getAccount(@PathVariable String email, 
	                                 @RequestHeader("Authorization") String authorization);
	    
	    @PostMapping("/bank-account")
	    ResponseEntity<?> createAccount(@RequestBody CreateBankAccountDto dto, 
	                                    @RequestHeader("Authorization") String authorization);
	    
	    @PutMapping("/bank-account/{email}")
	    ResponseEntity<?> updateAccount(@PathVariable String email, 
	                                    @RequestBody BankAccountDto dto, 
	                                    @RequestHeader("Authorization") String authorization);
	    
	    @DeleteMapping("/bank-account/{email}")
	    ResponseEntity<?> deleteAccount(@PathVariable String email, 
	                                    @RequestHeader("Authorization") String authorization);
	    
	    @PutMapping("/bank-account/{email}/currency/{currency}")
	    ResponseEntity<?> updateCurrencyAmount(@PathVariable String email,
	                                          @PathVariable String currency,
	                                          @RequestParam BigDecimal amount,
	                                          @RequestHeader("Authorization") String authorization);
	    
	    @PostMapping("/bank-account/{email}/exchange")
	    ResponseEntity<?> exchangeCurrency(
	            @PathVariable String email,
	            @RequestParam String fromCurrency,
	            @RequestParam String toCurrency,
	            @RequestParam BigDecimal amount,
	            @RequestParam BigDecimal exchangeRate,
	            @RequestHeader("Authorization") String authorization);
	    
	    @PostMapping("/bank-account/{email}/withdraw")
	    ResponseEntity<?> withdraw(
	            @PathVariable String email,
	            @RequestParam String currency,
	            @RequestParam BigDecimal amount,
	            @RequestHeader("Authorization") String authorization
	    );

	    @PostMapping("/bank-account/{email}/deposit")
	    ResponseEntity<?> deposit(
	            @PathVariable String email,
	            @RequestParam String currency,
	            @RequestParam BigDecimal amount,
	            @RequestHeader("Authorization") String authorization
	    );
	    
}