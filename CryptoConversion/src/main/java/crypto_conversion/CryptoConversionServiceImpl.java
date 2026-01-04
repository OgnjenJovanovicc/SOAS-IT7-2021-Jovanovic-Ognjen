package crypto_conversion;

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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.CryptoConversionRequestDto;
import api.dtos.CryptoConversionResponseDto;
import api.dtos.CryptoExchangeDto;
import api.dtos.UserDto;
import api.proxies.CryptoExchangeProxy;
import api.proxies.CryptoWalletProxy;
import api.proxies.UserProxy;
import api.services.CryptoConversionService;
import util.exceptions.InvalidQuantityException;
import util.exceptions.UnauthorizedRoleException;

@RestController
public class CryptoConversionServiceImpl implements CryptoConversionService {

    private static final Logger logger =
            LoggerFactory.getLogger(CryptoConversionServiceImpl.class);

    @Autowired
    private CryptoExchangeProxy exchangeProxy;

    @Autowired
    private UserProxy usersProxy;

    @Autowired
    private CryptoWalletProxy walletProxy;

    // ===================== GET =====================

    @Override
    @GetMapping("/crypto-conversion")
    public ResponseEntity<?> getCryptoConversion(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        logger.info("GET /crypto-conversion");

        if (authorization != null) {
            return performCryptoTransaction(from, to, amount, authorization);
        }

        return calculateCryptoOnly(from, to, amount);
    }

    @Override
    @GetMapping("/crypto-conversion-feign")
    public ResponseEntity<?> getCryptoConversionFeign(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        logger.info("GET /crypto-conversion-feign");

        if (authorization != null) {
            return performCryptoTransaction(from, to, amount, authorization);
        }

        return calculateCryptoOnly(from, to, amount);
    }

    // ===================== POST =====================

    @Override
    @PostMapping("/crypto-conversion")
    public ResponseEntity<?> convertCrypto(
            CryptoConversionRequestDto request,
            @RequestHeader("Authorization") String authorization) {

        return getCryptoConversion(
                request.getFrom(),
                request.getTo(),
                request.getAmount(),
                authorization
        );
    }

    // ===================== TRANSACTION =====================

    private ResponseEntity<?> performCryptoTransaction(
            String from,
            String to,
            BigDecimal amount,
            String authorization) {

        logger.info("=== PERFORMING CRYPTO TRANSACTION ===");

        validateAmount(amount);

        String email = decodeEmail(authorization);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid Authorization header");
        }

        String role = getUserRole(email, authorization);
        if (!"USER".equals(role)) {
            throw new UnauthorizedRoleException(
                    "Only USER role can perform crypto conversion. Your role: " + role);
        }

        CryptoExchangeDto exchange =
                exchangeProxy.getCryptoExchange(from, to).getBody();

        Map<String, Object> result = walletProxy.exchange(
                email,
                from,
                to,
                amount,
                exchange.getExchangeRate(),
                authorization
        );

        BigDecimal converted =
                amount.multiply(exchange.getExchangeRate());

        result.put("exchangeRate", exchange.getExchangeRate());
        result.put("fromCrypto", from);
        result.put("toCrypto", to);
        result.put("amount", amount);
        result.put("convertedAmount", converted);
        result.put("calculation",
                String.format("%s %s × %s = %s %s",
                        amount, from,
                        exchange.getExchangeRate(),
                        converted, to));

        String message = String.format(
                "Uspešno je izvršena razmena %s: %s za %s po kursu %s",
                from, amount, to, exchange.getExchangeRate()
        );

        return ResponseEntity.ok(
                new CryptoConversionResponseDto(result, message));
    }

    // ===================== CALCULATION ONLY =====================

    private ResponseEntity<?> calculateCryptoOnly(
            String from,
            String to,
            BigDecimal amount) {

        logger.info("=== CRYPTO CALCULATION ONLY ===");

        validateAmount(amount);

        CryptoExchangeDto exchange =
                exchangeProxy.getCryptoExchange(from, to).getBody();

        BigDecimal converted =
                amount.multiply(exchange.getExchangeRate());

        CryptoConversionRequestDto dto =
                new CryptoConversionRequestDto();

        dto.setFrom(from);
        dto.setTo(to);
        dto.setAmount(amount);
        dto.setExchangeRate(exchange.getExchangeRate());
        dto.setConvertedAmount(converted);
        dto.setCalculation(
                String.format("%s %s × %s = %s %s",
                        amount, from,
                        exchange.getExchangeRate(),
                        converted, to));

        return ResponseEntity.ok(dto);
    }

    // ===================== HELPERS =====================

    private void validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.valueOf(1000)) > 0) {
            throw new InvalidQuantityException(
                    "Crypto amount of " + amount + " is too large");
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
}
