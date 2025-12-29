package api.proxies;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import api.dtos.CurrencyExchangeDto;
@FeignClient(name="currency-exchange1", url="http://localhost:8000")
public interface CurrencyExchangeProxy {
	
	@GetMapping("/currency-exchange")
	ResponseEntity<CurrencyExchangeDto> getExchangeFeign(@RequestParam(value = "from") String from,  @RequestParam(value="to") String to);

}
