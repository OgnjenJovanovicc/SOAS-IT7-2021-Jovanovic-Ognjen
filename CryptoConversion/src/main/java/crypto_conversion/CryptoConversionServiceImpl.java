package crypto_conversion;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import api.dtos.*;
import api.proxies.CryptoExchangeProxy;
import api.services.CryptoConversionService;
import util.exceptions.UnauthorizedRoleException;


@RestController
public class CryptoConversionServiceImpl implements CryptoConversionService {

    private RestTemplate template = new RestTemplate();

    @Autowired
    private CryptoExchangeProxy proxy;

    @Autowired
    private AuthDecoder authDecoder;

    @Value("${crypto.wallet.service.url:http://localhost:8300}")
    private String cryptoWalletServiceUrl;

    @Value("${users.service.url:http://localhost:8770}")
    private String usersServiceUrl;

    @Override
    public ResponseEntity<?> convertCrypto(
            CryptoConversionRequestDto request,
            String authorizationHeader) {

        String email = authDecoder.decodeEmailFromAuthHeader(authorizationHeader);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid authorization header");
        }

        String role = getUserRole(email, authorizationHeader);
        if (!"USER".equals(role)) {
            throw new UnauthorizedRoleException(
                "Only USER role can perform crypto conversion. Your role: " + role
            );
        }

        CryptoExchangeDto exchangeRate =
                proxy.getCryptoExchange(request.getFrom(), request.getTo()).getBody();

        String exchangeUrl =
                cryptoWalletServiceUrl + "/crypto-wallet/" + email + "/exchange" +
                "?fromCrypto=" + request.getFrom() +
                "&toCrypto=" + request.getTo() +
                "&amount=" + request.getAmount() +
                "&exchangeRate=" + exchangeRate.getExchangeRate();

        try {
            ResponseEntity<Map> response = template.exchange(
                exchangeUrl,
                HttpMethod.POST,
                new HttpEntity<>(createHeaders(authorizationHeader)),
                Map.class
            );

            Map<String, Object> walletState = response.getBody();

            String message = String.format(
                "Uspešno je izvršena razmena %s: %s za %s po kursu %s",
                request.getFrom(),
                request.getAmount(),
                request.getTo(),
                exchangeRate.getExchangeRate()
            );

            CryptoConversionResponseDto dto =
                    new CryptoConversionResponseDto(walletState, message);

            return ResponseEntity.ok(dto);

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
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
