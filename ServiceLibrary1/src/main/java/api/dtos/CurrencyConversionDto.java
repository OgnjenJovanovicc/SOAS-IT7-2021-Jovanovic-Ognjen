package api.dtos;

import java.math.BigDecimal;

public class CurrencyConversionDto {

	private CurrencyExchangeDto exchange;
	private BigDecimal quantity;
	private ConversionResault conversionResault;
	private boolean feign;
	public CurrencyConversionDto() {
		
	}
	
public CurrencyConversionDto(CurrencyExchangeDto exchange,BigDecimal quantity) {
		this.exchange=exchange;
		this.quantity=quantity;
		CurrencyConversionDto.ConversionResault result=
				new CurrencyConversionDto.ConversionResault(exchange.getTo(), quantity.multiply(exchange.getExchangeRate()));
		this.conversionResault=result;
}
	
	
	
	public CurrencyExchangeDto getExchange() {
		return exchange;
	}



	public void setExchange(CurrencyExchangeDto exchange) {
		this.exchange = exchange;
	}



	public BigDecimal getQuantity() {
		return quantity;
	}



	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}



	public ConversionResault getConversionResault() {
		return conversionResault;
	}



	public void setConversionResault(ConversionResault conversionResault) {
		this.conversionResault = conversionResault;
	}



	public boolean isFeign() {
		return feign;
	}

	public void setFeign(boolean feign) {
		this.feign = feign;
	}



	private class ConversionResault{
		private String to;
		private BigDecimal convertedAmount;
		
		public ConversionResault() {
			
		}
		
		

		public ConversionResault(String to, BigDecimal convertedAmount) {
			this.to=to;
			this.convertedAmount=convertedAmount;
		}



		public String getTo() {
			return to;
		}

		public void setTo(String to) {
			this.to = to;
		}

		public BigDecimal getConvertedAmount() {
			return convertedAmount;
		}

		public void setConvertedAmount(BigDecimal convertedAmount) {
			this.convertedAmount = convertedAmount;
		}
		
		
	}
}
