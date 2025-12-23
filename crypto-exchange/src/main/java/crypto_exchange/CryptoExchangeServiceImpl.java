package crypto_exchange;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.CryptoExchangeDto;
import api.services.CryptoExchangeService;
import util.exceptions.CurrencyDoesntExistException;
import util.exceptions.NotDataFoundException;

@RestController
@Validated
public class CryptoExchangeServiceImpl implements CryptoExchangeService {

    @Autowired
    private CryptoExchangeRepository repo;

    @Autowired
    private Environment environment;

    @Override
    @GetMapping("/crypto-exchange")
    public ResponseEntity<?> getCryptoExchange(String from, String to) {

        String missing = null;
        List<String> valid = repo.findAllDistinctCryptos();

        if (!isValidCrypto(from)) missing = from;
        if (!isValidCrypto(to)) missing = to;

        if (missing != null) {
            throw new CurrencyDoesntExistException(
                    "Crypto currency " + missing + " does not exist",
                    valid);
        }

        CryptoExchangeModel model = repo.findByFromAndTo(from, to);

        if (model == null) {
            throw new NotDataFoundException(
                    "Requested crypto exchange [" + from + " → " + to + "] does not exist",
                    valid);
        }

        CryptoExchangeDto dto =
                new CryptoExchangeDto(model.getFrom(), model.getTo(), model.getExchangeRate());

        dto.setPort(environment.getProperty("local.server.port"));

        return ResponseEntity.ok(dto);
    }

    private boolean isValidCrypto(String crypto) {
        return repo.findAllDistinctCryptos()
                   .stream()
                   .anyMatch(c -> c.equalsIgnoreCase(crypto));
    }
}
