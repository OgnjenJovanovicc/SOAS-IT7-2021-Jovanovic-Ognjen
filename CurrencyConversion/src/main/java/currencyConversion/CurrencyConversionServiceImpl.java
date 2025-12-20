package currencyConversion;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import api.dtos.BankAccountDto;
import api.dtos.CurrencyConversionDto;
import api.dtos.CurrencyConversionRequestDto;
import api.dtos.CurrencyConversionResponseDto;
import api.dtos.CurrencyExchangeDto;
import api.dtos.UserDto;
import api.proxies.CurrencyExchangeProxy;
import api.services.CurrencyConversionService;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import util.exceptions.InsufficientFundsException;
import util.exceptions.InvalidQuantityException;
import util.exceptions.UnauthorizedRoleException;

@RestController
public class CurrencyConversionServiceImpl implements CurrencyConversionService {

	private RestTemplate template = new RestTemplate();
	
	@Autowired
	private CurrencyExchangeProxy proxy;
	
	@Value("${bank.account.service.url:http://localhost:8200}")
	private String bankAccountServiceUrl;
	
	Retry retry;
	CurrencyExchangeDto response;
	
	public CurrencyConversionServiceImpl(RetryRegistry registry) {
		retry=registry.retry("default");
	}
	
/*
	@Override
	@CircuitBreaker(name="cb",fallbackMethod="fallback")
	public ResponseEntity<?> getConversionFeign(String from, String to, BigDecimal quantity) {
		if(quantity.compareTo(BigDecimal.valueOf(300.0))==1) {
			throw new InvalidQuantityException(String.format("Qunatity of %s is to large", quantity));
		}
		
		retry.executeSupplier(() -> response = proxy.getExchangeFeign(from, to).getBody());
		
		
		CurrencyConversionDto finalResponse=new CurrencyConversionDto(response,quantity);
		finalResponse.setFeign(true);
		return ResponseEntity.ok(finalResponse);
	}
	
	public ResponseEntity<?> fallback(CallNotPermittedException ex){
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Currency conversion service is currently unavailbale,Circuit is in OPEN state!");
	}
	@Override
	public ResponseEntity<?> getConversion(String from, String to, BigDecimal quantity) {
		
		if(quantity.compareTo(BigDecimal.valueOf(300.0))==1) {
			throw new InvalidQuantityException(String.format("Qunatity of %s is to large", quantity));
		}
		String endPoint ="http://localhost:8000/currency-exchange?from="+ from + "&to=" +to;
	ResponseEntity<CurrencyExchangeDto>response = template.getForEntity(endPoint, CurrencyExchangeDto.class);
	return ResponseEntity.ok(new CurrencyConversionDto(response.getBody(),quantity));
	}


	@Override
	public ResponseEntity<?> convertCurrency(CurrencyConversionRequestDto request, String authorizationHeader) {
		// TODO Auto-generated method stub
		return null;
	}*/

    @Override
    @CircuitBreaker(name="cb",fallbackMethod="fallback")
    public ResponseEntity<?> getConversionFeign(String from, String to, BigDecimal quantity) {
        if(quantity.compareTo(BigDecimal.valueOf(300.0))==1) {
            throw new InvalidQuantityException(String.format("Qunatity of %s is to large", quantity));
        }
        
        retry.executeSupplier(() -> response = proxy.getExchangeFeign(from, to).getBody());
        
        CurrencyConversionDto finalResponse=new CurrencyConversionDto(response,quantity);
        finalResponse.setFeign(true);
        return ResponseEntity.ok(finalResponse);
    }
    
    public ResponseEntity<?> fallback(CallNotPermittedException ex){
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Currency conversion service is currently unavailbale,Circuit is in OPEN state!");
    }
    
    @Override
    public ResponseEntity<?> getConversion(String from, String to, BigDecimal quantity) {
        
        if(quantity.compareTo(BigDecimal.valueOf(300.0))==1) {
            throw new InvalidQuantityException(String.format("Qunatity of %s is to large", quantity));
        }
        String endPoint ="http://localhost:8000/currency-exchange?from="+ from + "&to=" +to;
        ResponseEntity<CurrencyExchangeDto>response = template.getForEntity(endPoint, CurrencyExchangeDto.class);
        return ResponseEntity.ok(new CurrencyConversionDto(response.getBody(),quantity));
    }


    @Override
    public ResponseEntity<?> convertCurrency(CurrencyConversionRequestDto request, String authorizationHeader) {
        
        // 1. Dekodiraj email iz Authorization headera
        String email = decodeEmail(authorizationHeader);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid authorization header");
        }
        
        System.out.println("DEBUG: User email: " + email);
        System.out.println("DEBUG: Converting " + request.getQuantity() + " " + request.getFrom() + 
                           " to " + request.getTo());
        
        // 2. Proveri rolu korisnika
        String role = getUserRole(email, authorizationHeader);
        System.out.println("DEBUG: User role: " + role);
        
        if (!"USER".equals(role)) {
            throw new UnauthorizedRoleException(
                "Only USER role can perform currency conversion. Your role: " + role
            );
        }
        
        // 3. Dobij kurs za razmenu
        CurrencyExchangeDto exchangeRate;
        try {
            exchangeRate = proxy.getExchangeFeign(request.getFrom(), request.getTo()).getBody();
            System.out.println("DEBUG: Exchange rate: " + exchangeRate.getExchangeRate());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot get exchange rate: " + e.getMessage());
        }
        
        // 4. Pozovi exchange endpoint na bank-account servisu
        String exchangeUrl = bankAccountServiceUrl + "/bank-account/" + email + "/exchange" +
                             "?fromCurrency=" + request.getFrom() +
                             "&toCurrency=" + request.getTo() +
                             "&amount=" + request.getQuantity() +
                             "&exchangeRate=" + exchangeRate.getExchangeRate();
        
        System.out.println("DEBUG: Calling bank-account exchange: " + exchangeUrl);
        
        try {
            // Pozovi bank-account servis
            ResponseEntity<Map> exchangeResponse = template.exchange(
                exchangeUrl,
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createHeaders(authorizationHeader)),
                Map.class
            );
            
            System.out.println("DEBUG: Bank-account response status: " + exchangeResponse.getStatusCode());
            
            // 5. Vrati odgovor direktno iz bank-account servisa
            return ResponseEntity.ok(exchangeResponse.getBody());
            
        } catch (HttpClientErrorException e) {
            // Ako bank-account servis vrati grešku (npr. 400, 403, 404)
            System.out.println("DEBUG: Bank-account error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Exchange failed: " + e.getResponseBodyAsString());
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied: " + e.getResponseBodyAsString());
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Account not found: " + e.getResponseBodyAsString());
            } else {
                return ResponseEntity.status(e.getStatusCode())
                        .body("Bank-account error: " + e.getResponseBodyAsString());
            }
            
        } catch (Exception e) {
            System.out.println("DEBUG: Unexpected error: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Currency conversion failed: " + e.getMessage());
        }
    }
    
    // Pomoćne metode
    
    private String decodeEmail(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                return null;
            }
            String base64 = authHeader.substring("Basic ".length());
            String credentials = new String(Base64.getDecoder().decode(base64));
            return credentials.split(":", 2)[0];
        } catch (Exception e) {
            return null;
        }
    }
    
    private String getUserRole(String email, String authHeader) {
        try {
            String url = "http://localhost:8770/users/email?email=" + email;
            
            // Pošalji zahtev sa Authorization headerom
            ResponseEntity<UserDto> response = template.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(createHeaders(authHeader)),
                UserDto.class
            );
            
            return response.getBody().getRole();
        } catch (Exception e) {
            return "ERROR";
        }
    }
    /*
    private BankAccountDto getBankAccount(String email, String authHeader) {
        try {
            String url = "http://localhost:8200/bank-account/" + email;
            
            ResponseEntity<BankAccountDto> response = template.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(createHeaders(authHeader)),
                BankAccountDto.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Cannot get bank account: " + e.getMessage());
        }
    }
    
    private void updateBankAccount(String email, BankAccountDto account, String authHeader) {
        try {
            String url = "http://localhost:8200/bank-account/" + email;
            
            template.exchange(
                url,
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(account, createHeaders(authHeader)),
                BankAccountDto.class
            );
        } catch (Exception e) {
            throw new RuntimeException("Cannot update bank account: " + e.getMessage());
        }
    }
    
    private BigDecimal getAmountByCurrency(BankAccountDto account, String currency) {
        if (currency == null) return BigDecimal.ZERO;
        
        switch (currency.toUpperCase()) {
            case "USD": return account.getUsdAmount() != null ? account.getUsdAmount() : BigDecimal.ZERO;
            case "EUR": return account.getEurAmount() != null ? account.getEurAmount() : BigDecimal.ZERO;
            case "GBP": return account.getGbpAmount() != null ? account.getGbpAmount() : BigDecimal.ZERO;
            case "CHF": return account.getChfAmount() != null ? account.getChfAmount() : BigDecimal.ZERO;
            case "RSD": return account.getRsdAmount() != null ? account.getRsdAmount() : BigDecimal.ZERO;
            default: return BigDecimal.ZERO;
        }
    }
    
    private void setAmountByCurrency(BankAccountDto account, String currency, BigDecimal amount) {
        if (currency == null || amount == null) return;
        
        switch (currency.toUpperCase()) {
            case "USD": account.setUsdAmount(amount); break;
            case "EUR": account.setEurAmount(amount); break;
            case "GBP": account.setGbpAmount(amount); break;
            case "CHF": account.setChfAmount(amount); break;
            case "RSD": account.setRsdAmount(amount); break;
        }
    }
    */
    private org.springframework.http.HttpHeaders createHeaders(String authHeader) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.set("Content-Type", "application/json");
        return headers;
    }
}
