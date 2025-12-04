package currencyExchange;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import api.dtos.CurrencyExchangeDto;
import api.services.CurrencyExchangeService;

@RestController
public class CurrencyExchangeServiceImpl implements CurrencyExchangeService {

	@Autowired
	private CurrencyExchangeRepository repo;
	
	@Override
	public ResponseEntity<?> getCurrencyExchange(String from, String to) {
		CurrencyExchangeModel dbResponese=repo.findByFromAndTo(from, to);
		if(dbResponese==null) {
			return new ResponseEntity("Unablae to find exchange rate FROM: "+ from+" TO"+ to,
					HttpStatus.NOT_FOUND);
		}
		CurrencyExchangeDto dto=new CurrencyExchangeDto(dbResponese.getTo(),dbResponese.getFrom(),dbResponese.getExchangeRate());
		return ResponseEntity.ok(dto);
	}
	
	//ZA PROBU2
	

}
