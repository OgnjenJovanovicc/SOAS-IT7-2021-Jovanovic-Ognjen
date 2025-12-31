package api.services;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import api.dtos.CurrencyConversionRequestDto;

@Service
public interface CurrencyConversionService {

	
	@GetMapping("/currency-conversion")
	ResponseEntity<?> getConversion(
	        @RequestParam String from,
	        @RequestParam String to,
	        @RequestParam BigDecimal quantity,
	        @RequestHeader(value = "Authorization", required = false) String authorizationHeader); 

	@GetMapping("/currency-conversion-feign")
	ResponseEntity<?> getConversionFeign(
	        @RequestParam String from,
	        @RequestParam String to,
	        @RequestParam BigDecimal quantity,
	        @RequestHeader(value = "Authorization", required = false) String authorizationHeader); 

	@PostMapping("/convert")
	ResponseEntity<?> convertCurrency(
        @RequestBody CurrencyConversionRequestDto request,
        @RequestHeader("Authorization") String authorizationHeader);
}
