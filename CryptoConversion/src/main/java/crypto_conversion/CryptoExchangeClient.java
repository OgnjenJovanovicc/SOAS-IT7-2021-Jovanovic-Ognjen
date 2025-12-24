package crypto_conversion;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import api.dtos.CryptoExchangeDto;

@Component
public class CryptoExchangeClient {

    private final RestTemplate restTemplate = new RestTemplate();

    public CryptoExchangeDto getRate(String from, String to) {
        return restTemplate.getForObject(
            "http://localhost:8400/crypto-exchange?from={from}&to={to}",
            CryptoExchangeDto.class,
            from, to
        );
    }
}
