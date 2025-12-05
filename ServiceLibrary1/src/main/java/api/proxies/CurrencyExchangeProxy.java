package api.proxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import api.dtos.CurrencyExchangeDto;
@FeignClient("currency-exchange1")
public interface CurrencyExchangeProxy {
	
	@GetMapping("/currency-exchange")
	ResponseEntity<CurrencyExchangeDto> getExchangeFeign(@RequestParam String from,  @RequestParam String to);



}
