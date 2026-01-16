package currencyConversion;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.CurrencyConversionDto;
import api.dtos.CurrencyConversionRequestDto;
import api.dtos.CurrencyExchangeDto;
import api.dtos.UserDto;
import api.proxies.BankAccountProxy;
import api.proxies.CurrencyExchangeProxy;
import api.proxies.UserProxy;
import api.services.CurrencyConversionService;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import util.exceptions.InvalidQuantityException;
import util.exceptions.UnauthorizedRoleException;

@RestController
public class CurrencyConversionServiceImpl implements CurrencyConversionService {

    private static final Logger logger =
            LoggerFactory.getLogger(CurrencyConversionServiceImpl.class);

    @Autowired
    private CurrencyExchangeProxy exchangeProxy;

    @Autowired
    private UserProxy usersProxy;

    @Autowired
    private BankAccountProxy bankAccountProxy;

    private final Retry retry;

    public CurrencyConversionServiceImpl(RetryRegistry registry) {
        this.retry = registry.retry("default");
    }

    // ===================== GET =====================

    @Override
    @GetMapping("/currency-conversion")
    @CircuitBreaker(name = "cb", fallbackMethod = "fallback")
    public ResponseEntity<?> getConversion(
            @RequestParam(value="from")String from,
            @RequestParam (value="to")String to,
            @RequestParam(value="quantity") BigDecimal quantity,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        logger.info("GET /currency-conversion");

        if (authorization != null) {
            return performTransaction(from, to, quantity, authorization);
        }

        return calculateOnly(from, to, quantity);
    }

    @Override
    @GetMapping("/currency-conversion-feign")
    @CircuitBreaker(name = "cb", fallbackMethod = "fallback")
    public ResponseEntity<?> getConversionFeign(
            @RequestParam(value="from") String from,
            @RequestParam(value="to") String to,
            @RequestParam (value="quantity")BigDecimal quantity,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        logger.info("GET /currency-conversion-feign");

        if (authorization != null) {
            return performTransaction(from, to, quantity, authorization);
        }

        return calculateOnly(from, to, quantity);
    }

    // ===================== POST =====================

    @Override
    @PostMapping("/convert")
    public ResponseEntity<?> convertCurrency(
            @RequestBody CurrencyConversionRequestDto request,
            @RequestHeader("Authorization") String authorization) {

        logger.info("POST /convert");

        return getConversion(
                request.getFrom(),
                request.getTo(),
                request.getQuantity(),
                authorization
        );
    }

    // ===================== TRANSACTION =====================

    private ResponseEntity<?> performTransaction(
            String from,
            String to,
            BigDecimal quantity,
            String authorization) {

        logger.info("=== PERFORMING REAL TRANSACTION ===");

        validateQuantity(quantity);

        String email = decodeEmail(authorization);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid Authorization header");
        }

        String role = getUserRole(email, authorization);
        if (!"USER".equals(role)) {
            throw new UnauthorizedRoleException(
                    "Only USER role can perform conversion. Your role: " + role);
        }

        CurrencyExchangeDto exchange =
                exchangeProxy.getExchangeFeign(from, to).getBody();

        Map<String, Object> result = bankAccountProxy.exchange(
                email,
                from,
                to,
                quantity,
                exchange.getExchangeRate(),
                authorization
        );

        BigDecimal converted =
                quantity.multiply(exchange.getExchangeRate());

        result.put("exchangeRate", exchange.getExchangeRate());
        result.put("fromCurrency", from);
        result.put("toCurrency", to);
        result.put("quantity", quantity);
        result.put("convertedAmount", converted);
        result.put("calculation",
                String.format("%s %s × %s = %s %s",
                        quantity, from,
                        exchange.getExchangeRate(),
                        converted, to));

        logger.info("=== TRANSACTION COMPLETED ===");

        return ResponseEntity.ok(result);
    }

    // ===================== CALCULATION ONLY =====================

    private ResponseEntity<?> calculateOnly(
            String from,
            String to,
            BigDecimal quantity) {

        logger.info("=== CALCULATION ONLY ===");

        validateQuantity(quantity);

        CurrencyExchangeDto response =
                retry.executeSupplier(() ->
                        exchangeProxy.getExchangeFeign(from, to).getBody());

        CurrencyConversionDto dto =
                new CurrencyConversionDto(response, quantity);
        dto.setFeign(true);

        return ResponseEntity.ok(dto);
    }

    // ===================== HELPERS =====================

    private void validateQuantity(BigDecimal quantity) {
        if (quantity.compareTo(BigDecimal.valueOf(300)) > 0) {
            throw new InvalidQuantityException(
                    "Quantity of " + quantity + " is too large");
        }
    }

    private String getUserRole(String email, String authorization) {
        try {
            UserDto user = usersProxy.getUser(email, authorization);
            return user.getRole();
        } catch (Exception e) {
            logger.error("Error getting user role", e);
            return "ERROR";
        }
    }

    private String decodeEmail(String authHeader) {
        try {
            if (!authHeader.startsWith("Basic ")) return null;
            String base64 = authHeader.substring(6);
            String decoded =
                    new String(Base64.getDecoder().decode(base64));
            return decoded.split(":", 2)[0];
        } catch (Exception e) {
            return null;
        }
    }

    // ===================== FALLBACK =====================

    public ResponseEntity<?> fallback(CallNotPermittedException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Currency conversion service unavailable (Circuit OPEN)");
    }
}



