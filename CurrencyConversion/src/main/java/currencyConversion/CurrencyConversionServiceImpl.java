package currencyConversion;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import api.dtos.CurrencyConversionDto;
import api.dtos.CurrencyConversionRequestDto;
import api.dtos.CurrencyExchangeDto;
import api.dtos.UserDto;
import api.proxies.CurrencyExchangeProxy;
import api.services.CurrencyConversionService;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import util.exceptions.InvalidQuantityException;
import util.exceptions.UnauthorizedRoleException;

@RestController
public class CurrencyConversionServiceImpl implements CurrencyConversionService {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyConversionServiceImpl.class);
    
    private RestTemplate template = new RestTemplate();
    
    @Autowired
    private CurrencyExchangeProxy proxy;
    
    @Value("${bank.account.service.url:http://localhost:8200}")
    private String bankAccountServiceUrl;
    
    @Value("${users.service.url:http://localhost:8770}")
    private String usersServiceUrl;
    
    Retry retry;
    CurrencyExchangeDto response;
    
    public CurrencyConversionServiceImpl(RetryRegistry registry) {
        retry = registry.retry("default");
    }
    
    @Override
    @GetMapping("/currency-conversion-feign")
    @CircuitBreaker(name="cb", fallbackMethod="fallback")
    public ResponseEntity<?> getConversionFeign(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal quantity,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        logger.info("GET /currency-conversion-feign called");
        
        if (authorizationHeader != null) {
            return performTransaction(from, to, quantity, authorizationHeader);
        }
        
        return calculateOnly(from, to, quantity);
    }
    
    @Override
    @GetMapping("/currency-conversion")
    @CircuitBreaker(name="cb", fallbackMethod="fallback")
    public ResponseEntity<?> getConversion(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal quantity,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        logger.info("GET /currency-conversion called");
        
        if (authorizationHeader != null) {
            return performTransaction(from, to, quantity, authorizationHeader);
        }

        return calculateOnly(from, to, quantity);
    }
    
    @Override
    @PostMapping("/convert")
    public ResponseEntity<?> convertCurrency(
            @RequestBody CurrencyConversionRequestDto request,
            @RequestHeader("Authorization") String authorizationHeader) {
        
        logger.info("POST /convert called");
        
        return getConversion(
            request.getFrom(), 
            request.getTo(), 
            request.getQuantity(), 
            authorizationHeader
        );
    }
    

    private ResponseEntity<?> performTransaction(String from, String to, BigDecimal quantity, String authorizationHeader) {
        logger.info("=== PERFORMING REAL TRANSACTION ===");
        logger.info("Converting: {} {} to {}", quantity, from, to);
        
        String email = decodeEmail(authorizationHeader);
        if (email == null) {
            logger.error("Failed to decode email from authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid authorization header");
        }
        
        logger.info("Decoded email: {}", email);

        String role = getUserRole(email, authorizationHeader);
        logger.info("User role: {}", role);
        
        if (!"USER".equals(role)) {
            logger.error("Unauthorized role: {}", role);
            throw new UnauthorizedRoleException(
                "Only USER role can perform currency conversion. Your role: " + role
            );
        }

        if (quantity.compareTo(BigDecimal.valueOf(300.0)) > 0) {
            throw new InvalidQuantityException(String.format("Quantity of %s is too large", quantity));
        }

        CurrencyExchangeDto exchangeRate;
        try {
            exchangeRate = proxy.getExchangeFeign(from, to).getBody();
            logger.info("Exchange rate: {}", exchangeRate.getExchangeRate());
        } catch (Exception e) {
            logger.error("Failed to get exchange rate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot get exchange rate: " + e.getMessage());
        }
        
        String exchangeUrl = bankAccountServiceUrl + "/bank-account/" + email + "/exchange" +
                             "?fromCurrency=" + from +
                             "&toCurrency=" + to +
                             "&amount=" + quantity +
                             "&exchangeRate=" + exchangeRate.getExchangeRate();
        
        logger.info("Calling bank-account service: {}", exchangeUrl);
        
        try {
            ResponseEntity<Map> exchangeResponse = template.exchange(
                exchangeUrl,
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(createHeaders(authorizationHeader)),
                Map.class
            );
            
            logger.info("Bank-account response status: {}", exchangeResponse.getStatusCode());
            logger.info("=== TRANSACTION COMPLETED SUCCESSFULLY ===");
            
            Map<String, Object> transactionResult = exchangeResponse.getBody();
            
            transactionResult.put("exchangeRate", exchangeRate.getExchangeRate());
            transactionResult.put("fromCurrency", from);
            transactionResult.put("toCurrency", to);
            transactionResult.put("quantity", quantity);
            
            BigDecimal convertedAmount = quantity.multiply(exchangeRate.getExchangeRate());
            transactionResult.put("convertedAmount", convertedAmount);
            transactionResult.put("calculation", 
                String.format("%s %s × %s = %s %s", 
                    quantity, from,
                    exchangeRate.getExchangeRate(),
                    convertedAmount,
                    to));
            
            return ResponseEntity.ok(transactionResult);
            
        } catch (HttpClientErrorException e) {
            logger.error("Bank-account HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            
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
            logger.error("Unexpected error: {}", e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Currency conversion failed: " + e.getMessage());
        }
    }
    
    private ResponseEntity<?> calculateOnly(String from, String to, BigDecimal quantity) {
        logger.info("=== CALCULATION ONLY (NO TRANSACTION) ===");
        logger.info("Calculating: {} {} to {}", quantity, from, to);
        
        if (quantity.compareTo(BigDecimal.valueOf(300.0)) > 0) {
            throw new InvalidQuantityException(String.format("Quantity of %s is too large", quantity));
        }
        
        retry.executeSupplier(() -> response = proxy.getExchangeFeign(from, to).getBody());
        
        CurrencyConversionDto finalResponse = new CurrencyConversionDto(response, quantity);
        finalResponse.setFeign(true);
             
        return ResponseEntity.ok(finalResponse);
    }

    public ResponseEntity<?> fallback(CallNotPermittedException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Currency conversion service is currently unavailable, Circuit is in OPEN state!");
    }

    
    private String decodeEmail(String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                return null;
            }
            String base64 = authHeader.substring("Basic ".length());
            String credentials = new String(Base64.getDecoder().decode(base64));
            return credentials.split(":", 2)[0];
        } catch (Exception e) {
            logger.error("Error decoding email: {}", e.getMessage());
            return null;
        }
    }
    
    private String getUserRole(String email, String authHeader) {
        try {
            String url = usersServiceUrl + "/users/email?email=" + email;
            
            ResponseEntity<UserDto> response = template.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(createHeaders(authHeader)),
                UserDto.class
            );
            
            return response.getBody().getRole();
            
        } catch (HttpClientErrorException e) {
            logger.error("Users service HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "ERROR";
        } catch (Exception e) {
            logger.error("Error getting user role: {}", e.getMessage());
            return "ERROR";
        }
    }
    
    private org.springframework.http.HttpHeaders createHeaders(String authHeader) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.set("Content-Type", "application/json");
        return headers;
    }
}