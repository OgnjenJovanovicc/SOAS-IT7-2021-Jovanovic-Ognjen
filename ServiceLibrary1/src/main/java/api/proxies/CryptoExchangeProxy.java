package api.proxies;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import api.dtos.CryptoExchangeDto;


@FeignClient(name = "crypto-exchange", url = "http://localhost:8400")
public interface CryptoExchangeProxy {

    @GetMapping("/crypto-exchange")
    ResponseEntity<CryptoExchangeDto> getCryptoExchange(
            @RequestParam String from,
            @RequestParam String to);
}