package api.services;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public interface CurrencyConversionService {

	
/*	@GetMapping("/currency-conversion-feign")
	ResponseEntity<?> getConversion(@RequestParam String from, @RequestParam String to, @RequestParam BigDecimal quantity);
	
	@GetMapping("/currency-conversion")
	ResponseEntity<?> getConversionFeign(@RequestParam String from, @RequestParam String to, @RequestParam BigDecimal quantity);*/
	
	@GetMapping("/currency-conversion")
	ResponseEntity<?> getConversion(
	        @RequestParam String from,
	        @RequestParam String to,
	        @RequestParam BigDecimal quantity);

	@GetMapping("/currency-conversion-feign")
	ResponseEntity<?> getConversionFeign(
	        @RequestParam String from,
	        @RequestParam String to,
	        @RequestParam BigDecimal quantity);
}
