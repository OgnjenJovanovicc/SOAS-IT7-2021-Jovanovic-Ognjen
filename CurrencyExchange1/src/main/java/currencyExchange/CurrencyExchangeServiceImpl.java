package currencyExchange;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.CurrencyExchangeDto;
import api.services.CurrencyExchangeService;
import util.exceptions.CurrencyDoesntExistException;
import util.exceptions.NotDataFoundException;

@RestController
@Validated
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {

	@Autowired
	private CurrencyExchangeRepository repo;
	
	@Autowired
	private Environment enviroment;
	@Override
	public ResponseEntity<?> getCurrencyExchange(String from, String to) {
		String missingCurrency=null;
		List<String>validCurencies=repo.findAllDistinctCurrencies();
		if(!isValidCurrency(from)) missingCurrency=from;
		
		if(!isValidCurrency(to)) missingCurrency=to;
		
		if(missingCurrency!=null) {
			throw new CurrencyDoesntExistException(String.format("Currency %s does not exist in the database", missingCurrency),
					validCurencies);
		}
		
		CurrencyExchangeModel dbResponese=repo.findByFromAndTo(from, to);
		if(dbResponese==null) {
			throw new NotDataFoundException(String.format("Requested exchange rate from [%s to %s] does not exist", from,to),
					validCurencies);
		}
		CurrencyExchangeDto dto=new CurrencyExchangeDto(dbResponese.getTo(),dbResponese.getFrom(),dbResponese.getExchangeRate());
		dto.setPort(enviroment.getProperty("local.server.port"));
		return ResponseEntity.ok(dto);
	}
	
	public boolean isValidCurrency(String currency) {
		List<String>currencies=repo.findAllDistinctCurrencies();
		for(String s: currencies) {
			if(s.equalsIgnoreCase(currency))
				return true;
		}
		return false;
	}
	
	
}
