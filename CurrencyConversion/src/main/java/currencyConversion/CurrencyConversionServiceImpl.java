package currencyConversion;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import api.dtos.CurrencyConversionDto;
import api.dtos.CurrencyExchangeDto;
import api.proxies.CurrencyExchangeProxy;
import api.services.CurrencyConversionService;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import util.exceptions.InvalidQuantityException;

@RestController
public class CurrencyConversionServiceImpl implements CurrencyConversionService {

	private RestTemplate template = new RestTemplate();
	
	@Autowired
	private CurrencyExchangeProxy proxy;
	
	Retry retry;
	CurrencyExchangeDto response;
	
	public CurrencyConversionServiceImpl(RetryRegistry registry) {
		retry=registry.retry("default");
	}
	

	@Override
	@CircuitBreaker(name="cb",fallbackMethod="fallback")
	public ResponseEntity<?> getConversionFeign(String from, String to, BigDecimal quantity) {
		if(quantity.compareTo(BigDecimal.valueOf(300.0))==1) {
			throw new InvalidQuantityException(String.format("Qunatity of %s is to large", quantity));
		}
		
		retry.executeSupplier(() -> response = proxy.getExchangeFeign(from, to).getBody());
		
		
		CurrencyConversionDto finalResponse=new CurrencyConversionDto(response,quantity);
		finalResponse.setFeign(true);
		return ResponseEntity.ok(finalResponse);
	}
	
	public ResponseEntity<?> fallback(CallNotPermittedException ex){
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Currency conversion service is currently unavailbale,Circuit is in OPEN state!");
	}
	@Override
	public ResponseEntity<?> getConversion(String from, String to, BigDecimal quantity) {
		
		if(quantity.compareTo(BigDecimal.valueOf(300.0))==1) {
			throw new InvalidQuantityException(String.format("Qunatity of %s is to large", quantity));
		}
		String endPoint ="http://localhost:8000/currency-exchange?from="+ from + "&to=" +to;
	ResponseEntity<CurrencyExchangeDto>response = template.getForEntity(endPoint, CurrencyExchangeDto.class);
	return ResponseEntity.ok(new CurrencyConversionDto(response.getBody(),quantity));
	}

}
