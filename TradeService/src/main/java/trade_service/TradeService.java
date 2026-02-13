package trade_service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import api.dtos.TradeRequest;
import api.dtos.UserDto;
import api.proxies.BankAccountProxy;
import api.proxies.CryptoWalletProxy;
import api.proxies.UserProxy;
import util.exceptions.UnauthorizedRoleException;

@Service
public class TradeService {

    private final AuthDecoder auth;
    private final UserProxy users;
    private final BankAccountProxy bank;
    private final CryptoWalletProxy wallet;
    private final CurrencyRateRepository rates;

    public TradeService(
            AuthDecoder auth,
            UserProxy users,
            BankAccountProxy bank,
            CryptoWalletProxy wallet,
            CurrencyRateRepository rates) {
        this.auth = auth;
        this.users = users;
        this.bank = bank;
        this.wallet = wallet;
        this.rates = rates;
    }

    public ResponseEntity<?> trade(TradeRequest request, String authHeader) {
        String email = auth.decodeEmail(authHeader);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        UserDto user = users.getUser(email, authHeader);
        if (!"USER".equals(user.getRole())) {
            throw new UnauthorizedRoleException("Only USER can trade");
        }

        String from = request.getFrom().toUpperCase();
        String to = request.getTo().toUpperCase();
        BigDecimal amount = request.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Amount must be > 0");
        }

        boolean fromCrypto = isCrypto(from);
        boolean toCrypto = isCrypto(to);

        if (fromCrypto && toCrypto) {
            return ResponseEntity.badRequest().body("Crypto→Crypto not supported in Trade service");
        }
        if (!fromCrypto && !toCrypto) {
            return ResponseEntity.badRequest().body("Fiat→Fiat not supported in Trade service");
        }

        return fromCrypto
                ? cryptoToFiat(email, from, to, amount, authHeader)
                : fiatToCrypto(email, from, to, amount, authHeader);
    }

    public ResponseEntity<?> tradeGet(String from, String to, BigDecimal amount, String authHeader) {
     /*   if (authHeader == null) {
            return calculateTradeOnly(from, to, amount);
        }
*/
        TradeRequest request = new TradeRequest();
        request.setFrom(from);
        request.setTo(to);
        request.setAmount(amount);

        return trade(request, authHeader);
    }
/*
    private ResponseEntity<?> calculateTradeOnly(String from, String to, BigDecimal amount) {
        try {
            String fromUpper = from.toUpperCase();
            String toUpper = to.toUpperCase();
            
            boolean fromCrypto = isCrypto(fromUpper);
            boolean toCrypto = isCrypto(toUpper);
            
            if (fromCrypto && toCrypto) {
                return ResponseEntity.badRequest()
                        .body("Crypto→Crypto not supported in Trade service");
            }
            if (!fromCrypto && !toCrypto) {
                return ResponseEntity.badRequest()
                        .body("Fiat→Fiat not supported in Trade service");
            }

            String intermediateFiat = determineIntermediateFiat(fromCrypto ? toUpper : fromUpper);
            BigDecimal rate = BigDecimal.ONE;
            BigDecimal finalAmount = amount;
            String calculationNote = "";
            
            if (fromCrypto) {
                // CRYPTO → FIAT
                rate = getRate(fromUpper, intermediateFiat);
                finalAmount = amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
                
                if (!toUpper.equals(intermediateFiat)) {
                    BigDecimal intermediateToFinalRate = getRate(intermediateFiat, toUpper);
                    finalAmount = finalAmount.multiply(intermediateToFinalRate).setScale(2, RoundingMode.HALF_UP);
                    calculationNote = String.format(" (preko %s)", intermediateFiat);
                }
                
            } else {
                // FIAT → CRYPTO
                if (!fromUpper.equals(intermediateFiat)) {
                    BigDecimal fromToIntermediateRate = getRate(fromUpper, intermediateFiat);
                    BigDecimal intermediateAmount = amount.multiply(fromToIntermediateRate)
                                                         .setScale(2, RoundingMode.HALF_UP);
                    rate = getRate(intermediateFiat, toUpper);
                    finalAmount = intermediateAmount.multiply(rate).setScale(8, RoundingMode.HALF_UP);
                    calculationNote = String.format(" (preko %s)", intermediateFiat);
                } else {
                    rate = getRate(intermediateFiat, toUpper);
                    finalAmount = amount.multiply(rate).setScale(8, RoundingMode.HALF_UP);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("from", fromUpper);
            response.put("to", toUpper);
            response.put("amount", amount);
            response.put("exchangeRate", rate);
            response.put("convertedAmount", finalAmount);
            response.put("intermediateCurrency", intermediateFiat);
            response.put("tradeType", fromCrypto ? "CRYPTO_TO_FIAT" : "FIAT_TO_CRYPTO");
            response.put("calculation", 
                String.format("%s %s → %s %s%s", 
                    amount, fromUpper, finalAmount, toUpper, calculationNote));
            response.put("note", "Calculation only - no transaction performed");
            response.put("success", true);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Calculation failed: " + e.getMessage());
            errorResponse.put("note", "This is calculation only - no funds were transferred");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
*/
    // ================= FIAT → CRYPTO =================
    private ResponseEntity<?> fiatToCrypto(
            String email,
            String fromFiat,
            String toCrypto,
            BigDecimal amount,
            String authHeader) {

        try {
            String intermediateFiat = determineIntermediateFiat(fromFiat);
            BigDecimal intermediateAmount = amount;
            boolean needsConversion = !fromFiat.equals(intermediateFiat);
            if (needsConversion) {
                BigDecimal rateFiatToIntermediate = getRate(fromFiat, intermediateFiat);
                intermediateAmount = amount.multiply(rateFiatToIntermediate)
                                           .setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal rateIntermediateToCrypto = getRate(intermediateFiat, toCrypto);
            BigDecimal cryptoAmount = intermediateAmount.multiply(rateIntermediateToCrypto)
                                                        .setScale(8, RoundingMode.HALF_UP);

            Map<String, Object> withdrawResult = toMap(bank.withdraw(email, fromFiat, amount, authHeader));
            if (!isSuccess(withdrawResult)) {
                return error("Failed to withdraw " + fromFiat + " from bank", withdrawResult);
            }

            Map<String, Object> depositResult = toMap(wallet.deposit(email, toCrypto, cryptoAmount, authHeader));
            if (!isSuccess(depositResult)) {
                bank.deposit(email, fromFiat, amount, authHeader);
                return error("Failed to deposit " + toCrypto + " to wallet", depositResult);
            }

            String message = buildFiatToCryptoMessage(fromFiat, toCrypto, amount, cryptoAmount,
                                                      intermediateFiat, needsConversion, rateIntermediateToCrypto);

            return successResponse("FIAT_TO_CRYPTO", fromFiat, toCrypto,
                    amount, cryptoAmount, rateIntermediateToCrypto,
                    intermediateFiat, depositResult, message);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Fiat to Crypt failed: Insufficient currency in bank account");
        }
    }

    // ================= CRYPTO → FIAT =================
    private ResponseEntity<?> cryptoToFiat(
            String email,
            String fromCrypto,
            String toFiat,
            BigDecimal amount,
            String authHeader) {

        try {
            String intermediateFiat = determineIntermediateFiat(toFiat);

            BigDecimal rateCryptoToIntermediate = getRate(fromCrypto, intermediateFiat);
            BigDecimal intermediateAmount = amount.multiply(rateCryptoToIntermediate)
                                                  .setScale(2, RoundingMode.HALF_UP);

            Map<String, Object> withdrawResult = toMap(wallet.withdraw(email, fromCrypto, amount, authHeader));
            if (!isSuccess(withdrawResult)) {
                return error("Failed to withdraw " + fromCrypto + " from wallet", withdrawResult);
            }

            Map<String, Object> depositResult;
            BigDecimal finalAmount = intermediateAmount;
            boolean needsConversion = !toFiat.equals(intermediateFiat);

            if (needsConversion) {
                BigDecimal rateIntermediateToFiat = getRate(intermediateFiat, toFiat);
                finalAmount = intermediateAmount.multiply(rateIntermediateToFiat)
                                               .setScale(2, RoundingMode.HALF_UP);
                depositResult = toMap(bank.deposit(email, toFiat, finalAmount, authHeader));
            } else {
                depositResult = toMap(bank.deposit(email, intermediateFiat, intermediateAmount, authHeader));
            }

            if (!isSuccess(depositResult)) {
                //.deposit(email, fromCrypto, amount, authHeader); 
                return error("Failed to deposit " + toFiat + " to bank", depositResult);
            }

            String message = buildCryptoToFiatMessage(fromCrypto, toFiat, amount, finalAmount,
                                                      intermediateFiat, needsConversion, rateCryptoToIntermediate);

            return successResponse("CRYPTO_TO_FIAT", fromCrypto, toFiat,
                    amount, finalAmount, rateCryptoToIntermediate,
                    intermediateFiat, depositResult, message);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Crypto to Fiat failed: Insufficient currency in crypto wallet");
        }
    }

    // ================= POMOĆNE METODE =================
    private String determineIntermediateFiat(String fiat) {
        if ("USD".equals(fiat) || "EUR".equals(fiat)) return fiat;
        return "USD"; 
    }

    private BigDecimal getRate(String from, String to) {
        return rates.findByFromCurrencyAndToCurrency(from, to)
                    .orElseThrow(() -> new RuntimeException("Exchange rate not found for " + from + " → " + to))
                    .getRate();
    }

    private boolean isCrypto(String c) {
        return "BTC".equals(c) || "ETH".equals(c) || "USDT".equals(c);
    }

    private boolean isSuccess(Map<String, Object> result) {
        return result != null && !"ERROR".equals(result.get("status")) && !result.containsKey("error");
    }

    private ResponseEntity<?> error(String message, Map<String, Object> details) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", message);
        response.put("details", details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private Map<String, Object> toMap(Object obj) {
        if (obj instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        } else if (obj instanceof ResponseEntity<?> resp && resp.getBody() instanceof Map<?, ?> mapBody) {
            return (Map<String, Object>) mapBody;
        } else {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("status", "ERROR");
            fallback.put("message", obj != null ? obj.toString() : "No response");
            return fallback;
        }
    }

    private String buildFiatToCryptoMessage(String fromFiat, String toCrypto,
                                            BigDecimal fiatAmount, BigDecimal cryptoAmount,
                                            String intermediateFiat, boolean needsConversion,
                                            BigDecimal cryptoRate) {
        if (needsConversion) {
            return String.format(
                    "Uspešno razmenjeno %s %s za %s %s (preko %s). Kurs: 1 %s = %s %s",
                    fiatAmount, fromFiat,
                    cryptoAmount, toCrypto,
                    intermediateFiat,
                    intermediateFiat, cryptoRate, toCrypto
            );
        } else {
            return String.format(
                    "Uspešno razmenjeno %s %s za %s %s. Kurs: 1 %s = %s %s",
                    fiatAmount, fromFiat,
                    cryptoAmount, toCrypto,
                    intermediateFiat, cryptoRate, toCrypto
            );
        }
    }

    private String buildCryptoToFiatMessage(String fromCrypto, String toFiat,
                                            BigDecimal cryptoAmount, BigDecimal fiatAmount,
                                            String intermediateFiat, boolean needsConversion,
                                            BigDecimal cryptoRate) {
        if (needsConversion) {
            return String.format(
                    "Uspešno razmenjeno %s %s za %s %s (preko %s). Kurs: 1 %s = %s %s",
                    cryptoAmount, fromCrypto,
                    fiatAmount, toFiat,
                    intermediateFiat,
                    fromCrypto, cryptoRate, intermediateFiat
            );
        } else {
            return String.format(
                    "Uspešno razmenjeno %s %s za %s %s. Kurs: 1 %s = %s %s",
                    cryptoAmount, fromCrypto,
                    fiatAmount, toFiat,
                    fromCrypto, cryptoRate, intermediateFiat
            );
        }
    }

    private ResponseEntity<?> successResponse(String type, String from, String to,
                                              BigDecimal sent, BigDecimal received,
                                              BigDecimal rate, String intermediateCurrency,
                                              Map<String, Object> state, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("type", type);
        response.put("from", from);
        response.put("to", to);
        response.put("sent", sent);
        response.put("received", received);
        response.put("rate", rate);
        response.put("intermediateCurrency", intermediateCurrency);
        response.put("state", state);
        response.put("message", message);
        return ResponseEntity.ok(response);
    }
}
