package crypto_conversion;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import api.dtos.*;
import api.proxies.CryptoExchangeProxy;
import api.services.CryptoConversionService;
import util.exceptions.InvalidQuantityException;
import util.exceptions.UnauthorizedRoleException;

@RestController
public class CryptoConversionServiceImpl implements CryptoConversionService {

    private static final Logger logger = LoggerFactory.getLogger(CryptoConversionServiceImpl.class);
    
    private RestTemplate template = new RestTemplate();

    @Autowired
    private CryptoExchangeProxy proxy;

    @Value("${crypto.wallet.service.url:http://localhost:8300}")
    private String cryptoWalletServiceUrl;

    @Value("${users.service.url:http://localhost:8770}")
    private String usersServiceUrl;

    @Override
    @GetMapping("/crypto-conversion")
    public ResponseEntity<?> getCryptoConversion(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        logger.info("GET /crypto-conversion called: {} {} to {}", amount, from, to);
 
        if (authorizationHeader != null) {
            return performCryptoTransaction(from, to, amount, authorizationHeader);
        }
    
        return calculateCryptoOnly(from, to, amount);
    }

    @Override
    @GetMapping("/crypto-conversion-feign")
    public ResponseEntity<?> getCryptoConversionFeign(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        logger.info("GET /crypto-conversion-feign called: {} {} to {}", amount, from, to);
        
        if (authorizationHeader != null) {
            return performCryptoTransaction(from, to, amount, authorizationHeader);
        }
        
        return calculateCryptoOnly(from, to, amount);
    }

    @Override
    @PostMapping("/crypto-conversion")
    public ResponseEntity<?> convertCrypto(
            CryptoConversionRequestDto request,
            String authorizationHeader) {
        
        logger.info("POST /crypto-conversion called");
      
        return getCryptoConversion(
            request.getFrom(),
            request.getTo(),
            request.getAmount(),
            authorizationHeader
        );
    }

    private ResponseEntity<?> performCryptoTransaction(
            String from, String to, BigDecimal amount, String authorizationHeader) {
        
        logger.info("=== PERFORMING CRYPTO TRANSACTION ===");
        logger.info("Converting: {} {} to {}", amount, from, to);
        
        String email = decodeEmail(authorizationHeader);
        if (email == null) {
            logger.error("Failed to decode email from authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid authorization header");
        }
        
        logger.info("Decoded email: {}", email);

        String role = getUserRole(email, authorizationHeader);
        logger.info("User role: {}", role);
        
        if (!"USER".equals(role)) {
            logger.error("Unauthorized role: {}", role);
            throw new UnauthorizedRoleException(
                "Only USER role can perform crypto conversion. Your role: " + role
            );
        }

        if (amount.compareTo(BigDecimal.valueOf(1000.0)) > 0) {
            throw new InvalidQuantityException(String.format("Crypto amount of %s is too large", amount));
        }

        CryptoExchangeDto exchangeRate;
        try {
            exchangeRate = proxy.getCryptoExchange(from, to).getBody();
            logger.info("Crypto exchange rate: {}", exchangeRate.getExchangeRate());
        } catch (Exception e) {
            logger.error("Failed to get crypto exchange rate: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot get crypto exchange rate: " + e.getMessage());
        }
        

        String exchangeUrl = cryptoWalletServiceUrl + "/crypto-wallet/" + email + "/exchange" +
                             "?fromCrypto=" + from +
                             "&toCrypto=" + to +
                             "&amount=" + amount +
                             "&exchangeRate=" + exchangeRate.getExchangeRate();
        
        logger.info("Calling crypto-wallet service: {}", exchangeUrl);
        
        try {
            ResponseEntity<Map> response = template.exchange(
                exchangeUrl,
                HttpMethod.POST,
                new HttpEntity<>(createHeaders(authorizationHeader)),
                Map.class
            );
            
            logger.info("Crypto-wallet response status: {}", response.getStatusCode());
            logger.info("=== CRYPTO TRANSACTION COMPLETED SUCCESSFULLY ===");
  
            Map<String, Object> transactionResult = response.getBody();

            transactionResult.put("exchangeRate", exchangeRate.getExchangeRate());
            transactionResult.put("fromCrypto", from);
            transactionResult.put("toCrypto", to);
            transactionResult.put("amount", amount);
            
            BigDecimal convertedAmount = amount.multiply(exchangeRate.getExchangeRate());
            transactionResult.put("convertedAmount", convertedAmount);
            transactionResult.put("calculation", 
                String.format("%s %s × %s = %s %s", 
                    amount, from,
                    exchangeRate.getExchangeRate(),
                    convertedAmount,
                    to));
   
            String message = String.format(
                "Uspešno je izvršena razmena %s: %s za %s po kursu %s",
                from, amount, to, exchangeRate.getExchangeRate()
            );
            transactionResult.put("message", message);

            CryptoConversionResponseDto responseDto = 
                    new CryptoConversionResponseDto(transactionResult, message);
            
            return ResponseEntity.ok(responseDto);
            
        } catch (HttpClientErrorException e) {
            logger.error("Crypto-wallet HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Crypto exchange failed: " + e.getResponseBodyAsString());
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied: " + e.getResponseBodyAsString());
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Crypto wallet not found: " + e.getResponseBodyAsString());
            } else {
                return ResponseEntity.status(e.getStatusCode())
                        .body("Crypto-wallet error: " + e.getResponseBodyAsString());
            }
            
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Crypto conversion failed: " + e.getMessage());
        }
    }
    
    private ResponseEntity<?> calculateCryptoOnly(String from, String to, BigDecimal amount) {
        logger.info("=== CRYPTO CALCULATION ONLY (NO TRANSACTION) ===");
        logger.info("Calculating: {} {} to {}", amount, from, to);
        
        if (amount.compareTo(BigDecimal.valueOf(1000.0)) > 0) {
            throw new InvalidQuantityException(String.format("Crypto amount of %s is too large", amount));
        }
        
        try {
            CryptoExchangeDto exchangeRate = proxy.getCryptoExchange(from, to).getBody();
            
            CryptoConversionRequestDto calculationDto = new CryptoConversionRequestDto();
            calculationDto.setFrom(from);
            calculationDto.setTo(to);
            calculationDto.setAmount(amount);
            calculationDto.setExchangeRate(exchangeRate.getExchangeRate());
            
            BigDecimal convertedAmount = amount.multiply(exchangeRate.getExchangeRate());
            calculationDto.setConvertedAmount(convertedAmount);
            
            calculationDto.setCalculation(
                String.format("%s %s × %s = %s %s", 
                    amount, from,
                    exchangeRate.getExchangeRate(),
                    convertedAmount,
                    to));
             
            return ResponseEntity.ok(calculationDto);
            
        } catch (Exception e) {
            logger.error("Failed to calculate crypto conversion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Cannot calculate crypto conversion: " + e.getMessage());
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
            logger.error("Error decoding email: {}", e.getMessage());
            return null;
        }
    }
    
    private String getUserRole(String email, String authHeader) {
        try {
            String url = usersServiceUrl + "/users/email?email=" + email;

            ResponseEntity<UserDto> response = template.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(createHeaders(authHeader)),
                UserDto.class
            );

            return response.getBody().getRole();
        } catch (Exception e) {
            logger.error("Error getting user role: {}", e.getMessage());
            return "ERROR";
        }
    }

    private HttpHeaders createHeaders(String authHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.set("Content-Type", "application/json");
        return headers;
    }
}